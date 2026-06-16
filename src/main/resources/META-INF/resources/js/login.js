// 1. 유효성 검사 및 로그인 시작 함수
function validateAndLogin() {
    let valid = true;
    const username = document.getElementById('usernameInput').value.trim();
    const password = document.getElementById('passwordInput').value;

    // ① 아이디 유효성 검사 (4~20자 영문/숫자)
    const usernameRegex = /^[a-zA-Z0-9]{4,20}$/;
    if (!usernameRegex.test(username)) {
        showError('usernameInput', 'usernameMsg', '아이디는 4~20자의 영문/숫자만 가능합니다.');
        valid = false;
    } else {
        clearError('usernameInput', 'usernameMsg');
    }

    // ② 패스워드 유효성 검사 (8자 이상, 영문+숫자+특수문자)
    const passwordRegex = /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[!@#$%^&*]).{8,}$/;
    if (!passwordRegex.test(password)) {
        showError('passwordInput', 'passwordMsg', '8자 이상, 영문+숫자+특수문자를 포함해야 합니다.');
        valid = false;
    } else {
        clearError('passwordInput', 'passwordMsg');
    }

    // ③ 검사 통과 시 암호화 및 제출 진행
    if (valid) {
        submitLogin();
    }
}

// 2. 암호화 후 폼 제출 함수
async function submitLogin() {
    const passwordInput = document.getElementById('passwordInput').value;
    const hashed = await hashPassword(passwordInput); // SHA-256 해시화
    
    // 폼 내의 실제 전송용 password 필드에 해시값 저장
    document.getElementById('password').value = hashed; 
    
    // 폼 제출
    document.getElementById('loginForm').submit();
}

// 3. 서버 리다이렉트 에러 처리 (강의자료 25페이지)
window.addEventListener('load', function() {
    const params = new URLSearchParams(window.location.search);
    const error = params.get('error');

    if (error === '1') {
        // 기존 오류 메시지 초기화 후 실패 메시지 출력
        clearError('passwordInput', 'passwordMsg');
        showError('passwordInput', 'passwordMsg', '아이디 또는 패스워드가 올바르지 않습니다.');
    }
});

window.addEventListener('load', function() {
const params = new URLSearchParams(window.location.search);
const error  = params.get('error');
if (error === '1') {
/* 여기에코드를작성하시오*/
// 힌트1 : showError() 함수재활용
// 힌트2 : 아이디와패스워드중
//          어느필드에메시지를표시할지 정
// 힌트3 : 메시지내용
//          "아이디또는패스워드가올바르지않습니다."
}
});