/* 기사 수정 화면 JS */

//기사 수정 (AJAX)
function submitForm(){
  const form = document.getElementById('engineerForm');
  const formData = new FormData(form);

  //FormData를 JSON 객체로 변환
  const data = {};
  formData.forEach((value, key) => {
    data[key] = value;
  });

  //engineerNo는 숫자로 변환
  data.engineerNo = parseInt(data.engineerNo);

  fetch('/engineer-api/update', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
    .then(response => response.json())
    .then(result => {
      if(result === true){
        alert("수정되었습니다.");
        location.href = '/admin/engineer';
      } else {
        alert("수정 실패");
      }
    });
}
