window.onload = function() {
    // 1. 서버에서 사용자 정보 요청
    fetch('/profile/info')
        .then(res => res.json())
        .then(data => {
            document.getElementById('infoUsername').textContent = data.username;
            document.getElementById('infoEmail').textContent = data.email;
            document.getElementById('infoPhone').textContent = data.phone;
            
            if (data.profileImage) {
                document.getElementById('profileImg').src = '/uploads/profile/' + data.profileImage;
            }

            document.getElementById('updateEmail').value = data.email;
            document.getElementById('updatePhone').value = data.phone;

            const profileLink = document.getElementById('profileNavLink');
            if (profileLink) {
                profileLink.setAttribute('data-bs-title', ' ' + data.username);
                new bootstrap.Tooltip(profileLink);
            }
        })
        .catch(error => console.error('Error fetching profile:', error));

    // 2. URL 파라미터 기반 알림 처리
    const params = new URLSearchParams(window.location.search);
    const error = params.get('error');
    const success = params.get('success');

    // [추가] 비밀번호 변경 성공 처리
    if (success === 'password_changed') {
        showToast('✅ 비밀번호가 변경 완료, 로그인 페이지로 이동합니다.', 'success');
        // 3.5초 후 로그인 페이지로 이동
        setTimeout(function() {
            window.location.href = '/logout?next=login';
        }, 3500);
    }

    // 기존 알림 처리 로직
    const msgEl = document.getElementById('updateMsg');
    if (success === 'updated') {
        msgEl.className = 'alert alert-success';
        msgEl.textContent = '개인정보가 수정되었습니다.';
    } else if (error === 'duplicate_email') {
        msgEl.className = 'alert alert-danger';
        msgEl.textContent = '이미 사용 중인 이메일입니다.';
    }

    if (error === 'wrong_password') {
        showToast('현재 비밀번호가 일치하지 않습니다.', 'danger');
        const pwMsgEl = document.getElementById('pwMsg');
        if (pwMsgEl) {
            pwMsgEl.className = 'alert alert-danger';
            pwMsgEl.textContent = '현재 비밀번호가 일치하지 않습니다.';
        }
    }
};

// [추가된 기능] SHA-256 해시 생성 함수
async function hashPassword(password) {
    const msgUint8 = new TextEncoder().encode(password);
    const hashBuffer = await crypto.subtle.digest('SHA-256', msgUint8);
    const hashArray = Array.from(new Uint8Array(hashBuffer));
    return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
}

// [추가된 기능] 비밀번호 유효성 검사 및 해시 처리
async function validateAndChangePassword() {
    let valid = true;
    const currentPw = document.getElementById('currentPwInput').value;
    const newPw = document.getElementById('newPwInput').value;
    const newPwConfirm = document.getElementById('newPwConfirm').value;

    if (!currentPw) {
        showFieldError('currentPwInput', 'currentPwMsg', '현재 비밀번호를 입력해주세요.');
        valid = false;
    } else {
        clearFieldError('currentPwInput');
    }

    const pwRegex = /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[!@#$%^&*]).{8,}$/;
    if (!pwRegex.test(newPw)) {
        showFieldError('newPwInput', 'newPwMsg', '8자 이상, 영문+숫자+특수문자를 포함해야 합니다.');
        valid = false;
    } else {
        clearFieldError('newPwInput');
    }

    if (newPw !== newPwConfirm) {
        showFieldError('newPwConfirm', 'newPwConfirmMsg', '새 비밀번호가 일치하지 않습니다.');
        valid = false;
    } else {
        clearFieldError('newPwConfirm');
    }

    if (!valid) return;

    const hashedCurrent = await hashPassword(currentPw);
    const hashedNew = await hashPassword(newPw);

    document.getElementById('currentPassword').value = hashedCurrent;
    document.getElementById('newPassword').value = hashedNew;

    document.getElementById('pwForm').submit();
}

// 유틸리티 함수들
function showFieldError(fieldId, msgId, message) {
    const field = document.getElementById(fieldId);
    field.classList.add('is-invalid');
    const msg = document.getElementById(msgId);
    if (msg) msg.textContent = message;
}

function clearFieldError(fieldId) {
    const field = document.getElementById(fieldId);
    field.classList.remove('is-invalid');
    field.classList.add('is-valid');
}

function showToast(message, type) {
    const toastEl = document.getElementById('liveToast');
    if (toastEl) {
        document.getElementById('toastBody').textContent = message;
        toastEl.className = `toast align-items-center text-white border-0 bg-${type === 'danger' ? 'danger' : 'success'}`;
        new bootstrap.Toast(toastEl).show();
    }
}