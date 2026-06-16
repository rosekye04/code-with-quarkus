document.addEventListener('DOMContentLoaded', () => {
    const btn = document.getElementById('themeToggleBtn');
    const body = document.body;
    const navbar = document.querySelector('.navbar');

    // 테마 적용 함수
    function applyTheme(isLight) {
        if (isLight) {
            body.classList.add('light-mode');
            btn.textContent = '☀ LIGHT';
            navbar.classList.remove('navbar-dark', 'bg-dark');
            navbar.classList.add('navbar-light', 'bg-light');
        } else {
            body.classList.remove('light-mode');
            btn.textContent = '🌙 DARK';
            navbar.classList.remove('navbar-light', 'bg-light');
            navbar.classList.add('navbar-dark', 'bg-dark');
        }
    }

    // 1. 저장된 테마 불러오기
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'light') {
        applyTheme(true);
    }

    // 2. 버튼 클릭 시 토글
    if (btn) {
        btn.addEventListener('click', () => {
            const isLight = body.classList.toggle('light-mode');
            localStorage.setItem('theme', isLight ? 'light' : 'dark');
            applyTheme(isLight);
        });
    }
});