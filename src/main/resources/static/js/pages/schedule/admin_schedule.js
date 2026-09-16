/*
  requestList / scheduleList 는 admin_schedule.html의 인라인 스크립트(th:inline="javascript")에서
  서버 데이터를 그대로 JS 전역 변수로 내려준 것을 사용합니다.
*/

const STATUS_COLORS = {
  RECEIVED: '#c95e69',
  ASSIGNED: '#4f8fc8',
  IN_PROGRESS: '#d88b27',
  COMPLETED: '#5f9968'
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
          displayOrder: r.status === 'RECEIVED' ? 0 : 1,
          extendedProps: {
            isAssigned: false,
            status: r.status,
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
        displayOrder: s.status === 'RECEIVED' ? 0 : 1,
        extendedProps: {
          isAssigned: true,
          status: s.status,
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

// 하루에 "접수완료"(RECEIVED, 빨강 - 아직 기사 배정 안 된 건이라 빨리 처리해야 하는 고객)가 아닌
// 일정이 몇 개까지 바로 보일지. 기사 홈 대시보드(result_dashboard.js의 capDashEventsPerDay)와
// 같은 방식으로, 이 개수까지는 그대로 보여주고 그 이상(초과분)만 "+N"으로 접습니다.
const MAX_VISIBLE_OTHER_PER_DAY = 1;

/**
 * 월간 달력(dayGridMonth) 전용 - 하루에 일정이 너무 많이 몰려서 그 날짜 칸 때문에
 * 달력 전체 높이가 늘어지는 걸 막기 위한 처리입니다.
 * RECEIVED(빨강, 미배정 접수 = 배정전)는 배정을 빨리 해줘야 하는 건이라 절대 생략하지 않고 전부 보여주고,
 * 그 외 상태(배정완료/진행중/완료 = 이미 배정된 건)는 하루 MAX_VISIBLE_OTHER_PER_DAY개까지만 칸에
 * 직접 보여주고, 그 초과분만 "+N"(N=그 날짜에 숨겨진 개수) 자리표시 이벤트 하나로 묶습니다.
 * 이 자리표시를 클릭하면(eventClick 참고) 날짜 이동 없이 숨겨진 일정들을 작은 목록(팝오버)으로 보여줍니다.
 * 일/주간 뷰(timeGridDay/timeGridWeek)에서는 칸 높이 문제가 없으므로 이 처리를 적용하지 않습니다.
 */
function capEventsPerDay(events) {
  const byDate = new Map();
  events.forEach(function (ev) {
    const dateKey = (ev.start || '').slice(0, 10);
    if (!byDate.has(dateKey)) byDate.set(dateKey, []);
    byDate.get(dateKey).push(ev);
  });

  const result = [];
  byDate.forEach(function (dayEvents, dateKey) {
    const redEvents = dayEvents.filter(function (ev) { return ev.extendedProps.status === 'RECEIVED'; });
    const otherEvents = dayEvents.filter(function (ev) { return ev.extendedProps.status !== 'RECEIVED'; });

    result.push.apply(result, redEvents);

    if (otherEvents.length <= MAX_VISIBLE_OTHER_PER_DAY) {
      result.push.apply(result, otherEvents);
      return;
    }

    // MAX_VISIBLE_OTHER_PER_DAY개는 그대로 보여주고, 그 초과분만 "+N"으로 접습니다.
    result.push.apply(result, otherEvents.slice(0, MAX_VISIBLE_OTHER_PER_DAY));

    const hiddenEvents = otherEvents.slice(MAX_VISIBLE_OTHER_PER_DAY);
    result.push({
      id: 'more-' + dateKey,
      start: dateKey,
      allDay: true,
      backgroundColor: 'transparent',
      borderColor: 'transparent',
      textColor: 'inherit',
      displayOrder: 2,
      classNames: ['fc-more-placeholder'],
      extendedProps: { isMoreLink: true, moreCount: hiddenEvents.length, moreDate: dateKey, hiddenEvents: hiddenEvents }
    });
  });

  return result;
}

/* ---------------- 하루 칸에 접힌 일정 목록 팝오버 ---------------- */
// "+N" 자리표시를 클릭했을 때, 날짜를 이동시키는 대신(기사 홈 대시보드와 통일된 방식으로)
// 접혀 있던 일정들을 작은 목록으로 그 자리에 바로 보여줍니다.
function removeMorePopover() {
  const existing = document.getElementById('calMorePopover');
  if (existing) existing.remove();
  document.removeEventListener('click', handleMorePopoverOutsideClick);
}

function handleMorePopoverOutsideClick(e) {
  const popover = document.getElementById('calMorePopover');
  if (popover && !popover.contains(e.target)) removeMorePopover();
}

function showMorePopover(anchorEl, hiddenEvents) {
  removeMorePopover();

  const popover = document.createElement('div');
  popover.id = 'calMorePopover';
  popover.className = 'cal-more-popover';

  hiddenEvents.forEach(function (ev) {
    const p = ev.extendedProps;
    const item = document.createElement('div');
    item.className = 'cal-more-popover-item';

    const dot = document.createElement('span');
    dot.className = 'cal-more-popover-dot';
    dot.style.backgroundColor = STATUS_COLORS[p.status] || '#9aa5ab';

    const text = document.createElement('div');
    text.className = 'cal-more-popover-text';

    const nameEl = document.createElement('div');
    nameEl.className = 'name';
    nameEl.textContent = p.customerName || '-';

    const subEl = document.createElement('div');
    subEl.className = 'sub';
    subEl.textContent = [p.engineerName, p.timeRange].filter(Boolean).join(' ') || (p.productType || '');

    text.appendChild(nameEl);
    text.appendChild(subEl);
    item.appendChild(dot);
    item.appendChild(text);

    item.addEventListener('click', function (e) {
      e.stopPropagation();
      removeMorePopover();
      console.log('일정 클릭:', p);
    });

    popover.appendChild(item);
  });

  document.body.appendChild(popover);

  const rect = anchorEl.getBoundingClientRect();
  popover.style.top = (rect.bottom + 4) + 'px';
  popover.style.left = rect.left + 'px';

  // 팝오버를 여는 클릭 자체가 곧바로 "바깥 클릭"으로 잡혀서 닫히지 않도록 다음 이벤트 루프에서 등록
  setTimeout(function () {
    document.addEventListener('click', handleMorePopoverOutsideClick);
  }, 0);
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
  let currentViewType = 'dayGridMonth';

  const calendar = new FullCalendar.Calendar(calendarEl, {
    initialView: 'dayGridMonth',
    locale: 'ko',
    height: 'auto',
    headerToolbar: false,
    eventDisplay: 'block',

    // 지원되는 속성 정렬 방식으로 접수완료 → 일반 일정 → "+N" 순서를 유지합니다.
    eventOrder: 'displayOrder',
    eventOrderStrict: true,

    events: function (fetchInfo, successCallback) {
      const raw = buildEvents(currentEngineerFilter);
      // 월간(dayGridMonth) 뷰에서만 하루 개수 제한을 적용합니다. 일/주간 뷰는 칸이 늘어지는
      // 문제가 없어서(시간축 스크롤) 전체를 다 보여줍니다.
      successCallback(currentViewType === 'dayGridMonth' ? capEventsPerDay(raw) : raw);
    },

    eventContent: function (arg) {
      const p = arg.event.extendedProps;

      // 하루에 몰린 일정을 접어둔 "+N" 자리표시 (capEventsPerDay 참고)
      if (p.isMoreLink) {
        const moreEl = document.createElement('div');
        moreEl.className = 'fc-more-cell';
        moreEl.textContent = '+' + p.moreCount;
        return { domNodes: [moreEl] };
      }

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
      const p = info.event.extendedProps;

      // "+N" 자리표시 클릭 - 기사 홈 대시보드와 통일된 방식으로, 날짜를 이동시키는 대신
      // 접혀 있던 일정들을 그 자리에서 작은 목록(팝오버)으로 보여줍니다.
      if (p.isMoreLink) {
        showMorePopover(info.el, p.hiddenEvents);
        return;
      }

      console.log('일정 클릭:', info.event.extendedProps);
    },

    datesSet: function (info) {
      currentViewType = info.view.type;
      document.getElementById('calTitle').textContent = info.view.title;
    }
  });

  calendar.render();

  document.getElementById('prevBtn').addEventListener('click', () => calendar.prev());
  document.getElementById('nextBtn').addEventListener('click', () => calendar.next());

  document.querySelectorAll('#viewSwitch button').forEach(function (btn) {
    btn.addEventListener('click', function () {
      currentViewType = btn.dataset.view;
      calendar.changeView(btn.dataset.view);
      document.querySelectorAll('#viewSwitch button').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
    });
  });

  document.getElementById('techFilter').addEventListener('change', function (e) {
    currentEngineerFilter = e.target.value;
    calendar.refetchEvents();
  });

  // ESC 키로 "+N" 목록 팝오버 닫기
  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') removeMorePopover();
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
