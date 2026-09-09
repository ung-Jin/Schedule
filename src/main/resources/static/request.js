document.addEventListener("DOMContentLoaded", function () {

  var form = document.getElementById("requestForm");
  var telInput = document.getElementById("customerTel");
  var wishDateInput = document.getElementById("wishDate");

  form.addEventListener("submit", function (e) {

    // 일단 자동 제출을 막아두고, 중복 확인이 끝난 뒤에 필요하면 직접 제출합니다.
    e.preventDefault();

    var customerTel = telInput.value;
    var wishDate = wishDateInput.value;

    // 비동기(AJAX)로 서버에 중복 여부만 먼저 물어봅니다.
    fetch("/request/checkDuplicate?customerTel=" + encodeURIComponent(customerTel)
        + "&wishDate=" + encodeURIComponent(wishDate))
      .then(function (response) {
        return response.json();
      })
      .then(function (data) {
        if (data.duplicate) {
          // 같은 연락처 + 같은 희망 날짜로 이미 접수된 요청이 있는 경우
          var proceed = confirm(
            "같은 연락처로 같은 희망 날짜에 이미 접수된 요청이 있습니다.\n그래도 접수하시겠습니까?"
          );
          if (proceed) {
            form.submit();
          }
        } else {
          // 중복이 아니면 그대로 접수
          form.submit();
        }
      })
      .catch(function (error) {
        console.log("중복 확인 중 오류가 발생했습니다:", error);
        // 확인 요청 자체가 실패해도 접수는 막지 않고 진행시킵니다.
        form.submit();
      });

  });

});
