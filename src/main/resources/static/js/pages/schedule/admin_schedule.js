/*
  requestList / scheduleList 는 admin_schedule.html의 인라인 스크립트(th:inline="javascript")에서
  서버 데이터를 그대로 JS 전역 변수로 내려준 것을 사용합니다. (이 파일 자체는 정적 파일이라 th: 표현식을 못 씀)
*/

// 상태별 배경색 — .schedule-footer 범례 아이콘 색과 동일하게 맞췄습니다.
const STATUS_COLORS = {
  RECEIVED: 'rgb(102, 163, 191)',
  ASSIGNED: 'rgb(51, 104, 160)',
  IN_PROGRESS: 'rgb(255, 157, 80)',
  COMPLETED: 'rgb(118, 196, 87)',
  CANCELED: 'rgb(231, 63, 30)'
};

/**
 * requestList + scheduleList를 requestNo 기준으로 병합해서 캘린더 이벤트 배열을 만듭니다.
 * - 기본은 requestList의 wishDate 자리에 표시
 * - 같은 requestNo로 배정된 스케줄이 scheduleList에 있으면, 그 데이터(fixDate/기사/상태)가 우선 적용되어
 *   wishDate 자리 대신 fixDate 자리에 표시됩니다.
 * - engineerFilter가 'all'이 아니면 해당 engineerNo로 배정된 일정만 남깁니다.
 */
function buildEvents(engineerFilter) {
  const merged = new Map();

  // 1) 전체 접수 목록을 wishDate 기준으로 먼저 깔아둠
  requestList.forEach(function (r) {
    merged.set(r.requestNo, {
      requestNo: r.requestNo,
      date: r.wishDate,
      allDay: true,
      engineerNo: null,
      engineerName: '',
      symptom: r.symptom || '',
      startTime: null,
      startTimeDisplay: '',
      endTime: null,
      status: r.status
    });
  });

  // 2) 배정된 스케줄로 덮어씀 — fixDate/기사/상태가 우선
  scheduleList.forEach(function (s) {
    const req = s.requestDTO || {};
    const eng = s.engineerDTO || {};
    merged.set(s.requestNo, {
      requestNo: s.requestNo,
      date: s.fixDate,
      allDay: false,
      engineerNo: s.engineerNo,
      engineerName: eng.engineerName || '',
      symptom: req.symptom || '',
      startTime: s.startTime,
      startTimeDisplay: (s.startTime || '').slice(0, 5),
      endTime: s.endTime,
      status: s.status
    });
  });

  let items = Array.from(merged.values()).filter(function (item) { return !!item.date; });

  // 3) 기사 필터 — 특정 기사를 선택하면 그 기사에게 배정된 일정만
  if (engineerFilter && engineerFilter !== 'all') {
    items = items.filter(function (item) {
      return String(item.engineerNo) === engineerFilter;
    });
  }

  // 4) FullCalendar 이벤트 형식으로 변환
  return items.map(function (item) {
    const color = STATUS_COLORS[item.status] || '#9aa5ab';
    return {
      id: String(item.requestNo),
      start: item.allDay ? item.date : (item.date + 'T' + (item.startTime || '00:00:00')),
      end: (!item.allDay && item.endTime) ? (item.date + 'T' + item.endTime) : undefined,
      allDay: item.allDay,
      backgroundColor: color,
      borderColor: color,
      textColor: '#fff',
      extendedProps: {
        requestNo: item.requestNo,
        engineerNo: item.engineerNo,
        engineerName: item.engineerName,
        symptom: item.symptom,
        startTimeDisplay: item.startTimeDisplay,
        status: item.status
      }
    };
  });
}

document.addEventListener('DOMContentLoaded', function () {

  /* ---------------- 캘린더 ---------------- */
  const calendarEl = document.getElementById('calendar');
  let currentEngineerFilter = 'all';

  const calendar = new FullCalendar.Calendar(calendarEl, {
    initialView: 'dayGridMonth',
    locale: 'ko',
    height: 'auto',
    headerToolbar: false,   // 기본 상단 UI를 끄고, 아래에서 직접 만든 버튼으로 조작

    events: function (fetchInfo, successCallback) {
      successCallback(buildEvents(currentEngineerFilter));
    },

    // 이벤트 안에 기사명 / 증상 / 시작시간을 세 줄로 표시
    eventContent: function (arg) {
      const p = arg.event.extendedProps;
      const lines = [];
      lines.push(p.engineerName || ' ');   // 미배정이면 빈 줄(공백) 유지
      lines.push(p.symptom || ' ');
      lines.push(p.startTimeDisplay || ' ');

      const wrap = document.createElement('div');
      wrap.className = 'fc-event-lines';
      lines.forEach(function (text, i) {
        const line = document.createElement('div');
        line.className = 'fc-event-line' + (i === 0 ? ' name' : '');
        line.textContent = text;
        wrap.appendChild(line);
      });
      return { domNodes: [wrap] };
    },

    eventClick: function (info) {
      // TODO: 필요하면 여기서 상세 팝업 등 연결
      console.log('일정 클릭:', info.event.extendedProps);
    },

    // 달이 바뀔 때마다 우리가 만든 제목(span#calTitle)을 갱신
    datesSet: function (info) {
      document.getElementById('calTitle').textContent = info.view.title;
    }
  });

  calendar.render();

  /* 이전 / 다음 이동 */
  document.getElementById('prevBtn').addEventListener('click', () => calendar.prev());
  document.getElementById('nextBtn').addEventListener('click', () => calendar.next());

  /* 일 / 주 / 월 보기 전환 */
  document.querySelectorAll('#viewSwitch button').forEach(function (btn) {
    btn.addEventListener('click', function () {
      calendar.changeView(btn.dataset.view);
      document.querySelectorAll('#viewSwitch button').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
    });
  });

  /* 기사 필터 — engineerNo(value) 기준으로 해당 기사의 배정 일정만 표시 */
  document.getElementById('techFilter').addEventListener('change', function (e) {
    currentEngineerFilter = e.target.value;
    calendar.refetchEvents();
  });

  /* ---------------- AS 일정 배정 폼 ---------------- */
  const assignForm = document.querySelector('.assign-card form');
  const assignBtn = assignForm.querySelector('button[type="button"]');
  const errorMsg = document.getElementById('error-msg');

  const requestSelect = assignForm.querySelector('select[name="requestNo"]');
  const engineerSelect = assignForm.querySelector('select[name="engineerNo"]');
  const dateInput = assignForm.querySelector('input[name="fixDate"]');
  const startTimeInput = assignForm.querySelector('input[name="startTime"]');
  const endTimeInput = assignForm.querySelector('input[name="endTime"]');

  assignBtn.addEventListener('click', function () {
    if (!requestSelect.value || !engineerSelect.value || !dateInput.value || !startTimeInput.value || !endTimeInput.value) {
      errorMsg.textContent = '모든 항목을 선택해주세요.';
      return;
    }
    if (startTimeInput.value >= endTimeInput.value) {
      errorMsg.textContent = '종료 시간은 시작 시간보다 늦어야 합니다.';
      return;
    }

    errorMsg.textContent = '';
    // action="/assign-schedule" 로 실제 POST — 서버(ScheduleController#assignSchedule)에서 저장 후
    // "/as-schedule"로 리다이렉트되면서 화면이 새로 그려집니다.
    assignForm.submit();
  });

});

//일정 배정하기 클릭시 함수 실행
const regSchedule = () => {
  document.querySelector('#schdeule-reg-form').submit();
}

//일정 배정하기 유효성 검사 