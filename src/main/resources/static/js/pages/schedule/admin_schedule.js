/*
  requestList / scheduleList 는 admin_schedule.html의 인라인 스크립트(th:inline="javascript")에서
  서버 데이터를 그대로 JS 전역 변수로 내려준 것을 사용합니다.
*/

const STATUS_COLORS = {
  RECEIVED: '#E98B50',
  ASSIGNED: '#2563eb',
  IN_PROGRESS: '#9a4c13',
  COMPLETED: '#25624e'
};

// "2026-09-15T14:00:00" 또는 "2026-09-15 14:00:00" 형태의 문자열에서 "14:00"만 추출.
// (DB startTime/endTime이 DATETIME이라 날짜+시간이 함께 내려오므로, 시간 부분만 뽑아 쓰기 위한 헬퍼)
function extractTimePart(dateTimeStr) {
  if (!dateTimeStr) return '';
  const sepIndex = dateTimeStr.indexOf('T') !== -1 ? dateTimeStr.indexOf('T') : dateTimeStr.indexOf(' ');
  if (sepIndex === -1) return dateTimeStr.slice(0, 5);
  return dateTimeStr.slice(sepIndex + 1, sepIndex + 6);
}

function buildEvents(engineerFilter) {
  const isAll = !engineerFilter || engineerFilter === 'all';
  const events = [];

  const assignedRequestNos = new Set(
    scheduleList
      .filter(function (s) { return s.requestNo !== null && s.requestNo !== undefined; })
      .map(function (s) { return String(s.requestNo); })
  );

  // 1) AS 접수 목록(requestList) — 아직 기사/시간이 없으므로 고객명/종류/증상만 표시
  if (isAll) {
    requestList
      .filter(function (r) { return !!r.wishDate; })
      .filter(function (r) { return !assignedRequestNos.has(String(r.requestNo)); })
      .forEach(function (r) {
        const color = STATUS_COLORS[r.status] || '#9aa5ab';
        events.push({
          id: 'req-' + r.requestNo,
          start: (r.wishDate || '').slice(0, 10),
          allDay: true,
          backgroundColor: color,
          borderColor: color,
          textColor: '#fff',
          extendedProps: {
            isAssigned: false,
            customerName: r.customerName || '',
            productType: r.productType || '',
            symptom: r.symptom || ''
          }
        });
      });
  }

  // 2) 배정된 스케줄(scheduleList) — 기사명 + 시간 추가
  scheduleList
    .filter(function (s) { return !!s.fixDate; })
    .filter(function (s) { return isAll || String(s.engineerNo) === engineerFilter; })
    .forEach(function (s) {
      const eng = s.engineerDTO || {};
      const req = s.requestDTO || {};
      const color = STATUS_COLORS[s.status] || '#9aa5ab';
      const startDisplay = extractTimePart(s.startTime || '');
      const endDisplay = extractTimePart(s.endTime || '');
      events.push({
        id: 'sch-' + s.scheduleNo,
        start: s.fixDate + 'T' + startDisplay,
        end: s.endTime ? (s.fixDate + 'T' + endDisplay) : undefined,
        allDay: false,
        backgroundColor: color,
        borderColor: color,
        textColor: '#fff',
        extendedProps: {
          isAssigned: true,
          customerName: req.customerName || '',
          productType: req.productType || '',
          symptom: req.symptom || '',
          engineerName: eng.engineerName || '',
          timeRange: startDisplay + (endDisplay ? ('~' + endDisplay) : '')
        }
      });
    });

  return events;
}

/**
 * 같은 기사(engineerNo)가 같은 날짜(fixDate)에 이미 배정받은 시간과 겹치는지 확인합니다.
 */
function findEngineerConflict(engineerNo, fixDate, startTime, endTime) {
  return scheduleList.find(function (s) {
    if (String(s.engineerNo) !== String(engineerNo)) return false;
    if (s.fixDate !== fixDate) return false;
    if (s.status === 'CANCELED') return false;

    const existingStart = extractTimePart(s.startTime || '');
    const existingEnd = extractTimePart(s.endTime || '');

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
    headerToolbar: false,
    eventDisplay: 'block',

    events: function (fetchInfo, successCallback) {
      successCallback(buildEvents(currentEngineerFilter));
    },

    eventContent: function (arg) {
      const p = arg.event.extendedProps;
      let lines;

      if (p.isAssigned) {
        // 배정된 일정: 고객명 / 종류·증상 / 기사명·시간
        lines = [
          p.customerName || ' ',
          [p.productType, p.symptom].filter(Boolean).join(' · ') || ' ',
          [p.engineerName, p.timeRange].filter(Boolean).join(' ') || ' '
        ];
      } else {
        // 미배정 접수: 고객명 / 종류 / 증상
        lines = [p.customerName || ' ', p.productType || ' ', p.symptom || ' '];
      }

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
      console.log('일정 클릭:', info.event.extendedProps);
    },

    datesSet: function (info) {
      document.getElementById('calTitle').textContent = info.view.title;
    }
  });

  calendar.render();

  document.getElementById('prevBtn').addEventListener('click', () => calendar.prev());
  document.getElementById('nextBtn').addEventListener('click', () => calendar.next());

  document.querySelectorAll('#viewSwitch button').forEach(function (btn) {
    btn.addEventListener('click', function () {
      calendar.changeView(btn.dataset.view);
      document.querySelectorAll('#viewSwitch button').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
    });
  });

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
  const dateInput = document.getElementById('fixDateInput');
  const startTimeInput = document.getElementById('startTimeInput');
  const endTimeInput = document.getElementById('endTimeInput');
  const startTimeHidden = document.getElementById('startTimeHidden');
  const endTimeHidden = document.getElementById('endTimeHidden');

  assignBtn.addEventListener('click', function () {
    if (!requestSelect.value || !engineerSelect.value || !dateInput.value || !startTimeInput.value || !endTimeInput.value) {
      errorMsg.textContent = '모든 항목을 선택해주세요.';
      return;
    }
    if (startTimeInput.value >= endTimeInput.value) {
      errorMsg.textContent = '종료 시간은 시작 시간보다 늦어야 합니다.';
      return;
    }

    const conflict = findEngineerConflict(engineerSelect.value, dateInput.value, startTimeInput.value, endTimeInput.value);
    if (conflict) {
      const engineerName = engineerSelect.options[engineerSelect.selectedIndex].textContent.trim();
      const conflictStart = extractTimePart(conflict.startTime || '');
      const conflictEnd = extractTimePart(conflict.endTime || '');
      errorMsg.textContent = engineerName + ' 기사님은 ' + conflictStart + ' ~ ' + conflictEnd + '에 다른 일정이 있어 배정할 수 없습니다.';
      return;
    }

    errorMsg.textContent = '';
    // fixDate + 시간을 합쳐서 LocalDateTime이 파싱 가능한 "yyyy-MM-ddTHH:mm" 형태로 채움
    startTimeHidden.value = dateInput.value + 'T' + startTimeInput.value;
    endTimeHidden.value = dateInput.value + 'T' + endTimeInput.value;

    assignForm.submit();
  });

});
