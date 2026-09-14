/*
  requestList / scheduleList 는 admin_schedule.html의 인라인 스크립트(th:inline="javascript")에서
  서버 데이터를 그대로 JS 전역 변수로 내려준 것을 사용합니다. (이 파일 자체는 정적 파일이라 th: 표현식을 못 씀)
*/

// 상태별 배경색 — .schedule-footer 범례 아이콘 색과 동일하게 맞췄습니다.
const STATUS_COLORS = {
  RECEIVED: 'rgb(102, 163, 191)',
  ASSIGNED: 'rgb(51, 104, 160)',
  IN_PROGRESS: 'rgb(255, 157, 80)',
  COMPLETED: 'rgb(118, 196, 87)'
};

/**
 * requestList와 scheduleList를 합쳐서 캘린더 이벤트로 만듭니다.
 * - engineerFilter가 'all'(전체 기사)이면:
 *     · scheduleList 전체를 fixDate 기준으로 표시 (engineerName / symptom / startTime 표시)
 *     · requestList는 wishDate 기준으로 표시하되, 이미 일정 배정(AS_SCHEDULE에 INSERT)되어
 *       scheduleList에도 같은 requestNo로 들어있는 건은 중복이므로 숨김
 * - engineerFilter가 특정 기사(engineerNo)면:
 *     · requestList는 전부 숨기고, scheduleList 중 그 engineerNo로 배정된 것만 fixDate 기준으로 표시
 */
function buildEvents(engineerFilter) {
  const isAll = !engineerFilter || engineerFilter === 'all';
  const events = [];

  // scheduleList에 이미 배정되어 있는 requestNo 집합 — "일정 배정하기"를 누르면 AS_SCHEDULE에
  // INSERT되면서 이 목록에 잡히므로, requestList 쪽 동일 requestNo는 중복 표시하지 않습니다.
  const assignedRequestNos = new Set(
    scheduleList
      .filter(function (s) { return s.requestNo !== null && s.requestNo !== undefined; })
      .map(function (s) { return String(s.requestNo); })
  );

  // 1) AS 접수 목록(requestList) — wishDate 기준. '전체 기사'일 때만 표시하고,
  //    scheduleList에 이미 배정된 requestNo는 제외합니다.
  if (isAll) {
    requestList
      .filter(function (r) { return !!r.wishDate; })
      .filter(function (r) { return !assignedRequestNos.has(String(r.requestNo)); })
      .forEach(function (r) {
        const color = STATUS_COLORS[r.status] || '#9aa5ab';
        events.push({
          id: 'req-' + r.requestNo,
          // wishDate가 'yyyy-MM-dd HH:mm:ss'(공백 구분, LocalDateTime 형태)로 내려오는 경우가 있어
          // allDay 이벤트에는 날짜 부분(앞 10자리)만 사용합니다. ('T'가 섞여 와도 동일하게 처리됨)
          start: (r.wishDate || '').slice(0, 10),
          allDay: true,
          backgroundColor: color,
          borderColor: color,
          textColor: '#fff',
          extendedProps: {
            engineerName: '',
            symptom: r.symptom || '',
            startTimeDisplay: '',
            status: r.status
          }
        });
      });
  }

  // 2) 배정된 스케줄(scheduleList) — fixDate 기준. engineerName / symptom / startTime은
  //    전부 scheduleList(및 그 안의 engineerDTO/requestDTO)에서 가져옵니다.
  //    기사가 선택되면 그 engineerNo의 것만 남깁니다.
  scheduleList
    .filter(function (s) { return !!s.fixDate; })
    .filter(function (s) { return isAll || String(s.engineerNo) === engineerFilter; })
    .forEach(function (s) {
      const eng = s.engineerDTO || {};
      const req = s.requestDTO || {};
      const color = STATUS_COLORS[s.status] || '#9aa5ab';
      events.push({
        id: 'sch-' + s.scheduleNo,
        start: s.fixDate + 'T' + (s.startTime || '00:00:00'),
        end: s.endTime ? (s.fixDate + 'T' + s.endTime) : undefined,
        allDay: false,
        backgroundColor: color,
        borderColor: color,
        textColor: '#fff',
        extendedProps: {
          engineerName: eng.engineerName || '',
          symptom: req.symptom || '',
          startTimeDisplay: (s.startTime || '').slice(0, 5),
          status: s.status
        }
      });
    });

  return events;
}

/**
 * 같은 기사(engineerNo)가 같은 날짜(fixDate)에 이미 배정받은 시간과 겹치는지 확인합니다.
 * scheduleList(페이지 로드 시 서버가 내려준 배정 목록)를 기준으로 검사하며, 취소(CANCELED)된
 * 일정은 검사 대상에서 제외합니다. 겹치는 일정이 있으면 그 스케줄 객체를, 없으면 null을 반환합니다.
 */
function findEngineerConflict(engineerNo, fixDate, startTime, endTime) {
  return scheduleList.find(function (s) {
    if (String(s.engineerNo) !== String(engineerNo)) return false;
    if (s.fixDate !== fixDate) return false;
    if (s.status === 'CANCELED') return false;

    const existingStart = (s.startTime || '').slice(0, 5);
    const existingEnd = (s.endTime || '').slice(0, 5);

    // 시간 범위가 겹치는지: 새 시작 < 기존 종료  &&  기존 시작 < 새 종료
    return startTime < existingEnd && existingStart < endTime;
  }) || null;
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

    // scheduleList 이벤트는 allDay:false(시간 있는 일정)라서, 기본값(eventDisplay:'auto')이면
    // 월간뷰에서 작은 점(dot) 스타일로 렌더링되어 커스텀 eventContent(3줄 표시)가 거의 안 보이게 됩니다.
    // 'block'으로 고정하면 requestList(allDay 이벤트)와 동일하게 꽉 찬 박스로 렌더링됩니다.
    eventDisplay: 'block',

    events: function (fetchInfo, successCallback) {
      successCallback(buildEvents(currentEngineerFilter));
    },

    // 이벤트 안에 기사명 / 증상 / 시작시간을 세 줄로 표시
    eventContent: function (arg) {
      const p = arg.event.extendedProps;
      const lines = [];
      lines.push(p.engineerName || ' ');   // 미배정이면 빈 줄(공백) 유지
      lines.push(p.symptom || ' ');
      lines.push(p.startTimeDisplay || ' ');

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
    // 종료 시간이 시작 시간보다 늦어야 정상 — 그렇지 않을 때(같거나 이전일 때)만 에러 처리합니다.
    if (startTimeInput.value >= endTimeInput.value) {
      errorMsg.textContent = '종료 시간은 시작 시간보다 늦어야 합니다.';
      return;
    }

    // 같은 기사가 같은 날짜에 이미 겹치는 시간으로 배정되어 있으면 막습니다.
    const conflict = findEngineerConflict(engineerSelect.value, dateInput.value, startTimeInput.value, endTimeInput.value);
    if (conflict) {
      const engineerName = engineerSelect.options[engineerSelect.selectedIndex].textContent.trim();
      const conflictStart = (conflict.startTime || '').slice(0, 5);
      const conflictEnd = (conflict.endTime || '').slice(0, 5);
      errorMsg.textContent = engineerName + ' 기사님은 ' + conflictStart + ' ~ ' + conflictEnd + '에 다른 일정이 있어 배정할 수 없습니다.';
      return;
    }

    errorMsg.textContent = '';
    // action="/assign-schedule" 로 실제 POST — 서버(ScheduleController#assignSchedule)에서 저장 후
    // "/as-schedule"로 리다이렉트되면서 화면이 새로 그려집니다.
    assignForm.submit();
  });

});
