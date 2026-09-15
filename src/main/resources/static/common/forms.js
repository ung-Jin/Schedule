(function () {
  'use strict';

  const pickerSelector = [
    'input[type="date"]',
    'input[type="time"]',
    'input[type="datetime-local"]',
    'input[type="month"]',
    'input[type="week"]'
  ].join(',');

  function openPicker(input) {
    if (!input || input.disabled || input.readOnly) return;

    input.focus({ preventScroll: true });
    if (typeof input.showPicker === 'function') {
      try {
        input.showPicker();
      } catch (error) {
        // 지원하지 않는 환경에서는 브라우저의 기본 입력 동작을 그대로 사용합니다.
      }
    }
  }

  document.addEventListener('click', function (event) {
    const clickedInput = event.target.closest(pickerSelector);
    if (clickedInput) {
      openPicker(clickedInput);
      return;
    }

    const pickerField = event.target.closest('[data-picker-field]');
    if (!pickerField) return;

    openPicker(pickerField.querySelector(pickerSelector));
  });
})();
