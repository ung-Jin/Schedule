/* 기사관리 화면 JS (목록 + 등록/수정 통합) */

//현재 폼 모드 ('insert' 또는 'update')
let formMode = 'insert';

//등록 폼 열기
function openForm(){
  formMode = 'insert';

  //폼 초기화
  resetForm();

  //폼 UI 설정
  document.getElementById('formTitle').textContent = '기사 등록';
  document.getElementById('submitBtn').textContent = '등록하기';

  //아이디/비밀번호 노출 & 필수 설정
  document.getElementById('accountRow').style.display = '';
  document.getElementById('memId').required = true;
  document.getElementById('memPw').required = true;

  //폼 표시 + 스크롤
  const formCard = document.getElementById('formCard');
  formCard.style.display = 'block';
  formCard.scrollIntoView({ behavior: 'smooth' });
}

//수정 폼 열기 (기존 데이터 로드)
function openEditForm(engineerNo){
  formMode = 'update';

  //서버에서 기사 정보 조회
  fetch('/engineer-api/detail/' + engineerNo)
    .then(response => response.json())
    .then(engineer => {
      //폼 초기화
      resetForm();

      //폼 UI 설정
      document.getElementById('formTitle').textContent = '기사 수정';
      document.getElementById('submitBtn').textContent = '수정하기';

      //아이디/비밀번호 숨김 (수정 시 변경 불가)
      document.getElementById('accountRow').style.display = 'none';
      document.getElementById('memId').required = false;
      document.getElementById('memPw').required = false;

      //hidden값에 engineerNo 설정
      document.getElementById('engineerNo').value = engineer.engineerNo;

      //기존 값 세팅
      document.getElementById('engineerName').value = engineer.engineerName;
      document.getElementById('engineerTel').value  = engineer.engineerTel;
      document.getElementById('workStartTime').value = engineer.workStartTime;
      document.getElementById('workEndTime').value   = engineer.workEndTime;

      //전문분야 체크박스 세팅 (콤마 문자열을 분리해서 체크)
      if(engineer.specialty){
        const specialties = engineer.specialty.split(',');
        document.querySelectorAll('input[name="specialty"]').forEach(checkbox => {
          checkbox.checked = specialties.includes(checkbox.value);
        });
      }

      //상태 라디오 세팅
      document.querySelectorAll('input[name="status"]').forEach(radio => {
        radio.checked = (radio.value === engineer.status);
      });

      //폼 표시 + 스크롤
      const formCard = document.getElementById('formCard');
      formCard.style.display = 'block';
      formCard.scrollIntoView({ behavior: 'smooth' });
    });
}

//폼 닫기 (초기화 포함)
function closeForm(){
  document.getElementById('formCard').style.display = 'none';
  resetForm();
}

//폼 값 초기화
function resetForm(){
  document.getElementById('engineerForm').reset();
  document.getElementById('engineerNo').value = '';
}

//기사 삭제 (AJAX)
function deleteEngineer(engineerNo) {
  if(!confirm("정말 삭제하시겠습니까?")) return;

  fetch('/engineer-api/delete/' + engineerNo)
    .then(response => response.json())
    .then(result => {
      if(result === true){
        alert("삭제되었습니다.");
        location.reload();
      } else {
        alert("삭제 실패");
      }
    });
}

//기사 등록/수정 (AJAX)
function submitForm(){
  const form = document.getElementById('engineerForm');

  //체크박스로 선택된 전문분야를 콤마로 이어붙이기
  const checkedSpecialties = form.querySelectorAll('input[name="specialty"]:checked');
  const specialtyList = [];
  checkedSpecialties.forEach(checkbox => {
    specialtyList.push(checkbox.value);
  });
  const specialty = specialtyList.join(',');

  //공통 데이터
  const data = {
    engineerName : form.engineerName.value,
    engineerTel  : form.engineerTel.value,
    specialty    : specialty,
    workStartTime: form.workStartTime.value,
    workEndTime  : form.workEndTime.value,
    status       : form.status.value
  };

  if(formMode === 'insert'){
    //등록 시에만 아이디/비밀번호/회원 이름 필요
    data.memId   = form.memId.value;
    data.memPw   = form.memPw.value;
    data.memName = form.engineerName.value; //회원 이름 = 기사 이름

    //필수값 체크
    if(!data.memId || !data.memPw || !data.engineerName || !data.engineerTel){
      alert("아이디, 비밀번호, 기사명, 연락처는 필수 입력 항목입니다.");
      return;
    }

    sendRequest('/engineer-api/insert', data, "등록되었습니다.", "등록 실패");
  } else {
    //수정 시에는 engineerNo 필요
    data.engineerNo = parseInt(document.getElementById('engineerNo').value);

    //필수값 체크
    if(!data.engineerName || !data.engineerTel){
      alert("기사명, 연락처는 필수 입력 항목입니다.");
      return;
    }

    sendRequest('/engineer-api/update', data, "수정되었습니다.", "수정 실패");
  }
}

//공통 요청 처리
function sendRequest(url, data, successMsg, failMsg){
  fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
    .then(response => response.json())
    .then(result => {
      if(result === true){
        alert(successMsg);
        location.reload();
      } else {
        alert(failMsg);
      }
    });
}

//페이지 로드 시 폼 숨기기
window.addEventListener('DOMContentLoaded', () => {
  document.getElementById('formCard').style.display = 'none';
});
