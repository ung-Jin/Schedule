// 대시보드 "이번 달 일정" 달력.
// 날짜 칸을 통째로 색칠하는 대신, 일정 하나하나를 칸 안에 짧은 정보 블록(고객명/시간/주소/연락처)으로
// 보여주고 클릭하면 고객 기본정보 모달을 띄웁니다. (예전엔 바로 결과보고 화면으로 이동시켰지만,
// 달력은 빠르게 훑어보는 용도라 여기서는 조회/전화연결만 하고, 실제 결과보고는 "오늘의 일정" 카드에서 하도록 분리함)
// FullCalendar가 보여주는 달이 바뀔 때마다 events 콜백이 그 달의 연/월로 /as-result-calendar를 호출해서
// 그때그때 새로 받아옵니다. (admin_schedule.js의 eventContent/eventClick 패턴을 그대로 따릅니다)
// 일정 상태(접수완료/기사배정/작업진행/작업완료)에 따라 다른 색을 씁니다.
// 관리자 "일정관리" 화면(admin_schedule.js의 STATUS_COLORS)과 반드시 같은 색을 써서 통일성 있게 맞춥니다.
// (이 달력은 이미 나에게 배정된 일정만 보여주므로 실제로는 ASSIGNED/IN_PROGRESS/COMPLETED만 나오지만,
// 혹시 모를 다른 상태값도 대비해 RECEIVED 색도 같이 정의해둡니다)
var STATUS_COLORS = {
  RECEIVED: '#c95e69',
  ASSIGNED: '#4f8fc8',
  IN_PROGRESS: '#d88b27',
  COMPLETED: '#5f9968'
};

/* ---------------- 고객 기본정보 모달 ---------------- */

// 달력 이벤트(extendedProps)를 받아서 모달에 채워 넣고 엽니다. "오늘의 일정"의 진행예정 카드를 클릭했을 때도
// 이 함수를 그대로 재사용합니다 (result_dashboard.html의 인라인 스크립트 참고).
// startTime: 이 일정의 방문 시작 시각(Date). "시간이 지났다"는 종료 시각이 아니라 시작 시각 기준으로 판단합니다
// (오늘의 일정 카드와 동일한 규칙 - result_dashboard.html의 startTime.isAfter(now) 로직 참고).
function openCustomerModal(props, startTime) {
  const modal = document.getElementById('customerInfoModal');
  if (!modal) return;

  document.getElementById('modalCustomerName').textContent = props.customerName || '-';
  document.getElementById('modalCustomerTel').textContent = props.customerTel || '-';
  document.getElementById('modalCustomerAddr').textContent = props.customerAddr || '-';
  document.getElementById('modalTimeRange').textContent = props.timeRange || '-';

  // 전화연결 버튼 - tel: 링크로 바로 통화 연결 (연락처가 없으면 버튼 자체를 숨김)
  const callBtn = document.getElementById('modalCallBtn');
  if (props.customerTel) {
    // tel: 링크는 숫자/+ 외의 문자(하이픈 등)가 있어도 대부분 동작하지만, 안전하게 숫자/+만 남겨서 연결
    callBtn.href = 'tel:' + props.customerTel.replace(/[^0-9+]/g, '');
    callBtn.hidden = false;
  } else {
    callBtn.hidden = true;
  }

  // 방문 시작 시각이 지났는지 (진행예정 여부 판단 기준 - 종료 시각이 아니라 시작 시각!)
  const timeOver = !!startTime && startTime <= new Date();

  // "결과보고 보러가기" 버튼 - 완료(COMPLETED)된 일정만 결과가 등록되어 있으므로, 그때만 보여줌
  const viewBtn = document.getElementById('modalViewResultBtn');
  if (viewBtn) {
    if (props.status === 'COMPLETED') {
      viewBtn.href = '/as-result-view?scheduleNo=' + props.scheduleNo;
      viewBtn.hidden = false;
    } else {
      viewBtn.hidden = true;
    }
  }

  // "결과보고 작성하기" 버튼 - 완료 전이면서 방문 시작 시각이 지난(=오늘의 일정 카드에서 "결과보고" 상태인) 경우에만 보여줌.
  // 아직 방문 시작 전(진행예정)이면 이 버튼도 숨기고, 기존처럼 정보 확인 + 전화연결만 하는 모달로 둡니다.
  const writeBtn = document.getElementById('modalWriteResultBtn');
  if (writeBtn) {
    if (props.status !== 'COMPLETED' && timeOver) {
      writeBtn.href = '/as-result-report?scheduleNo=' + props.scheduleNo;
      writeBtn.hidden = false;
    } else {
      writeBtn.hidden = true;
    }
  }

  modal.hidden = false;
}

function closeCustomerModal() {
  const modal = document.getElementById('customerInfoModal');
  if (modal) modal.hidden = true;
}

document.addEventListener('DOMContentLoaded', function () {
  const calendarEl = document.getElementById('dashCalendar');
  if (!calendarEl) return;

  const calendar = new FullCalendar.Calendar(calendarEl, {
    initialView: 'dayGridMonth',
    locale: 'ko',
    height: 'auto',
    // 기본값(true)은 달과 상관없이 항상 6주치 칸을 그려서, 이번 달이 5주만에 끝나도
    // 다음 달 날짜만 있는 마지막 한 줄이 통째로 남아 달력이 쓸데없이 길어집니다.
    // false로 하면 그 달에 실제로 필요한 주(대부분 5주, 어떤 달은 4주)만큼만 그립니다.
    fixedWeekCount: false,
    headerToolbar: { left: 'prev', center: 'title', right: 'next' },
    // 기본값(auto)은 시간이 있는 이벤트를 월간뷰에서 점(dot) 하나로만 그려서 배경색이 안 보입니다.
    // 우리는 색으로 지난/오늘/예정을 구분해야 하니 항상 색칠된 박스로 그리게 강제합니다.
    eventDisplay: 'block',
    // 한 칸에 일정이 3개 이상 몰리면 특정 주만 유난히 늘어지므로, 2개까지만 보여주고
    // 나머지는 "+N개" 링크(클릭하면 팝오버로 전체 목록)로 접습니다.
    dayMaxEvents: 2,
    moreLinkText: function (n) { return '+' + n + '개'; },

    events: function (fetchInfo, successCallback, failureCallback) {
      // fetchInfo.start ~ end는 달력 그리드 전체 범위라 앞/뒤 달 날짜가 며칠 섞여 있을 수 있어서,
      // 그 범위의 한가운데 날짜로 "지금 실제로 보여주는 달"을 구합니다.
      const midDate = new Date((fetchInfo.start.getTime() + fetchInfo.end.getTime()) / 2);
      const year = midDate.getFullYear();
      const month = midDate.getMonth() + 1;

      fetch('/as-result-calendar?year=' + year + '&month=' + month)
        .then(function (res) { return res.json(); })
        .then(function (schedules) {
          successCallback(schedules.map(function (s) {
            const startDisplay = (s.startTime || '').slice(11, 16);
            const endDisplay = (s.endTime || '').slice(11, 16);
            const color = STATUS_COLORS[s.status] || '#9aa5ab';
            return {
              id: String(s.scheduleNo),
              start: s.startTime,
              end: s.endTime,
              backgroundColor: color,
              borderColor: color,
              textColor: '#fff',
              extendedProps: {
                scheduleNo: s.scheduleNo,
                customerName: s.customerName,
                customerTel: s.customerTel,
                customerAddr: s.customerAddr,
                timeRange: startDisplay + '~' + endDisplay,
                status: s.status
              }
            };
          }));
        })
        .catch(function () {
          successCallback([]);
        });
    },

    // 칸 안에 고객명 / 시간 / 주소 / 연락처를 네 줄로 짧게 표시 (긴 글자는 말줄임표 처리, 전체 내용은 title 툴팁으로)
    eventContent: function (arg) {
      const p = arg.event.extendedProps;
      const lines = [p.customerName, p.timeRange, p.customerAddr, p.customerTel];

      const wrap = document.createElement('div');
      wrap.className = 'dash-event-lines';
      wrap.title = lines.join(', ');
      lines.forEach(function (text, i) {
        const line = document.createElement('div');
        line.className = 'dash-event-line' + (i === 0 ? ' name' : '');
        line.textContent = text || ' ';
        wrap.appendChild(line);
      });
      return { domNodes: [wrap] };
    },

    // 일정 블록을 클릭하면 결과보고 화면으로 이동하는 대신, 고객 기본정보 모달을 띄웁니다.
    // info.event.start(방문 시작 시각)를 같이 넘겨서, 모달 안에서 "작성/보러가기" 버튼 노출 여부를 판단합니다.
    eventClick: function (info) {
      openCustomerModal(info.event.extendedProps, info.event.start);
    }
  });

  calendar.render();

  /* ---------------- 고객 기본정보 모달 닫기 ---------------- */
  const customerModal = document.getElementById('customerInfoModal');
  const customerModalCloseBtn = document.getElementById('customerModalClose');

  if (customerModalCloseBtn) {
    customerModalCloseBtn.addEventListener('click', closeCustomerModal);
  }
  if (customerModal) {
    // 모달 바깥(반투명 배경) 클릭 시 닫기 - 실제 내용 박스(.modal-box) 클릭은 무시
    customerModal.addEventListener('click', function (e) {
      if (e.target === customerModal) closeCustomerModal();
    });
  }
  // ESC 키로도 닫기
  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') closeCustomerModal();
  });
});
