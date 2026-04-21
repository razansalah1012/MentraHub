"use strict";

(function () {
  function applyTheme(darkMode, fontSize) {
    if (!document.body) return;
    var isDark = darkMode === true || darkMode === 'true';

    if (isDark) {
      document.body.classList.add('dark-mode');
    } else {
      document.body.classList.remove('dark-mode');
    }

    document.body.classList.remove('font-small', 'font-medium', 'font-large');

    if (fontSize) {
      document.body.classList.add('font-' + fontSize);
    }
  }

  function loadTheme() {
    var contextPath = window.contextPath || '/mentraApp/';
    fetch(contextPath + 'settings/preferences', {
      method: 'GET',
      credentials: 'same-origin'
    }).then(function (response) {
      return response.json();
    }).then(function (data) {
      if (data.success) {
        applyTheme(data.darkMode, data.fontSize);
        sessionStorage.setItem('darkMode', data.darkMode ? 'true' : 'false');
        sessionStorage.setItem('fontSize', data.fontSize || 'medium');
      }
    })["catch"](function (error) {
      console.log('Theme load error:', error);
    });
  }

  function init() {
    var savedDarkMode = sessionStorage.getItem('darkMode');
    var savedFontSize = sessionStorage.getItem('fontSize') || 'medium';

    if (savedDarkMode !== null) {
      applyTheme(savedDarkMode, savedFontSize);
    }

    loadTheme();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
//# sourceMappingURL=theme.dev.js.map
