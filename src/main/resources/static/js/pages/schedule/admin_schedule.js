  //  DB/DTO가 아직 정해지지 않아 events는 빈 배열로 시작합니다.
  //  나중에 API가 준비되면 아래 "TODO" 표시된 부분만 바꿔주면 됩니다. 
 
document.addEventListener('DOMContentLoaded', function () {
 
  /* ---------------- 캘린더 ---------------- */
  const calendarEl = document.getElementById('calendar');
 
  const calendar = new FullCalendar.Calendar(calendarEl, {
    initialView: 'dayGridMonth',
    locale: 'ko',
    height: 'auto',
    headerToolbar: false,   // 기본 상단 UI를 끄고, 아래에서 직접 만든 버튼으로 조작
 
    events: [],
    // TODO: 일정 테이블/DTO가 정해지면 아래처럼 교체
    // events: function (fetchInfo, successCallback, failureCallback) {
    //   fetch(`/api/schedules?start=${fetchInfo.startStr}&end=${fetchInfo.endStr}`)
    //     .then(res => res.json())
    //     .then(successCallback)
    //     .catch(failureCallback);
    // },
 
    eventClick: function (info) {
      // TODO: 일정 상세 팝업 등 연결
      console.log('일정 클릭:', info.event);
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
 
  /* 기사 필터 */
  document.getElementById('techFilter').addEventListener('change', function (e) {
    // TODO: 선택된 기사 기준으로 캘린더 이벤트 표시/숨김 처리
    // 예) calendar.getEvents().forEach(ev => ev.setProp('display', ...))
    console.log('선택된 기사:', e.target.value);
  });
 
  /* ---------------- AS 일정 배정 폼 ---------------- */
  const assignForm = document.querySelector('.assign-card form');
  const assignBtn = assignForm.querySelector('button[type="button"]');
 
  // 현재 select/input에 id가 없어서 구조(순서) 기준으로 찾습니다.
  // 1번째 select = AS 접수 선택, 2번째 select = 배정할 기사
  const [requestSelect, engineerSelect] = assignForm.querySelectorAll('select');
  const dateInput = assignForm.querySelector('input[type="date"]');
  const [startTimeInput, endTimeInput] = assignForm.querySelectorAll('input[type="time"]');
 
  assignBtn.addEventListener('click', function () {
    const payload = {
      requestNo: requestSelect.value,
      engineerNo: engineerSelect.value,
      visitDate: dateInput.value,
      startTime: startTimeInput.value,
      endTime: endTimeInput.value
    };
 
    if (!payload.requestNo || !payload.engineerNo || !payload.visitDate || !payload.startTime || !payload.endTime) {
      alert('모든 항목을 선택해주세요.');
      return;
    }
    if (payload.startTime >= payload.endTime) {
      alert('종료 시간은 시작 시간보다 늦어야 합니다.');
      return;
    }
 
    // TODO: 엔드포인트/DTO가 정해지면 아래 fetch로 교체
    console.log('일정 배정 요청:', payload);
    /*
    fetch('/api/schedules', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
      .then(res => {
        if (!res.ok) throw new Error('배정 실패');
        return res.json();
      })
      .then(() => {
        calendar.refetchEvents();
        assignForm.reset();
      })
      .catch(err => alert(err.message));
    */
  });
 
});