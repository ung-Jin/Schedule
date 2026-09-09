function deleteEngineer(engineerNo) {
  if (!confirm("정말 삭제하시겠습니까?")) return;
  axios.get('/engineer-api/delete/' + engineerNo)
    .then(response => {
      if (response.data === true) {
        alert("삭제되었습니다.");
        location.reload();
      } else {
        alert("삭제 실패");
      }
    })
    .catch(error => {
      console.error(error);
      alert("삭제 중 오류가 발생했습니다.");
    });
}

// 기사 등록
function submitForm() {
  const form = document.getElementById('engineerForm');
  const formData = new FormData(form);
  const data = {};
  formData.forEach((value, key) => {
  // 전문분야는 체크박스 여러 개 선택 가능
    if (key === 'specialty') {
      if (!data.specialty) {
        data.specialty = [];
      }
        data.specialty.push(value);
       } else {
        data[key] = value;
       }
    });
  axios.post('/engineer-api/insert', data)
       .then(response => {
    if (response.data === true) {
      alert("등록되었습니다.");
      location.reload();
    } else {
      alert("등록 실패");
      }
    })
      .catch(error => {
        console.error(error);
        alert("등록 중 오류가 발생했습니다.");
    });
}