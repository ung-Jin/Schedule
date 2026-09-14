// 대시보드 "이번 달 일정" 달력.
// 날짜 칸을 통째로 색칠하는 대신, 일정 하나하나를 칸 안에 짧은 정보 블록(고객명/시간/주소/연락처)으로
// 보여주고 클릭하면 바로 그 일정의 결과보고 화면으로 이동시킵니다.
// FullCalendar가 보여주는 달이 바뀔 때마다 events 콜백이 그 달의 연/월로 /as-result-calendar를 호출해서
// 그때그때 새로 받아옵니다. (admin_schedule.js의 eventContent/eventClick 패턴을 그대로 따릅니다)
// 일정 날짜가 오늘 기준으로 지난 일정 / 오늘 일정 / 예정된 일정인지에 따라 다른 색을 씁니다.
// (오늘 일정 색은 왼쪽 "오늘의 일정" 목록의 시간 표시 색(#2878e8)과 맞춰서 서로 같은 의미임을 알 수 있게 했습니다)
var DASH_EVENT_COLORS = { past: '#9aa5ab', today: '#2878e8', future: '#0ea5a4' };

function dashEventColor(startTimeStr) {
  const start = new Date(startTimeStr);
  const startDay = new Date(start.getFullYear(), start.getMonth(), start.getDate());
  const today = new Date();
  const todayDay = new Date(today.getFullYear(), today.getMonth(), today.getDate());

  if (startDay.getTime() === todayDay.getTime()) return DASH_EVENT_COLORS.today;
  return startDay < todayDay ? DASH_EVENT_COLORS.past : DASH_EVENT_COLORS.future;
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
            const color = dashEventColor(s.startTime);
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
                timeRange: startDisplay + '~' + endDisplay
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

    // 일정 블록을 클릭하면 그 일정의 결과보고 화면으로 바로 이동
    eventClick: function (info) {
      location.href = '/as-result-report?scheduleNo=' + info.event.extendedProps.scheduleNo;
    }
  });

  calendar.render();
});
