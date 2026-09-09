document.addEventListener("DOMContentLoaded", function () {

  var memIdInput = document.getElementById("memId");
  var saveIdCheckbox = document.getElementById("saveId");
  var pwInput = document.getElementById("memPw");
  var pwToggleBtn = document.getElementById("pwToggleBtn");
  var loginForm = document.getElementById("loginForm");

  // 1. 페이지가 열릴 때, 예전에 저장해둔 아이디가 있으면 자동으로 채워주기
  var savedId = localStorage.getItem("savedMemId");
  if (savedId) {
    memIdInput.value = savedId;
    saveIdCheckbox.checked = true;
  }

  // 2. 비밀번호 보이기/숨기기 버튼
  pwToggleBtn.addEventListener("click", function () {
    if (pwInput.type === "password") {
      pwInput.type = "text";
      pwToggleBtn.textContent = "🙈";
    } else {
      pwInput.type = "password";
      pwToggleBtn.textContent = "👁";
    }
  });

  // 3. 로그인 버튼을 눌러서 폼을 제출할 때 "아이디 저장" 체크 여부 처리
  //    (폼 제출 자체는 막지 않고, 그대로 서버(/member/login)로 전송됨)
  loginForm.addEventListener("submit", function () {
    if (saveIdCheckbox.checked) {
      localStorage.setItem("savedMemId", memIdInput.value);
    } else {
      localStorage.removeItem("savedMemId");
    }
  });

});
