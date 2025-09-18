<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>회원가입</title>
    <!-- CSS 완전 제거, 기본 HTML만 사용 -->
    <style>
        body { font-family: Arial; margin: 20px; }
        input, button { margin: 5px; padding: 8px; }
        button { cursor: pointer; }
        .feedback { margin: 5px 0; padding: 5px; }
        .valid { background: #d4edda; color: #155724; }
        .invalid { background: #f8d7da; color: #721c24; }
    </style>
</head>
<body>
    <h2>헬킹 회원가입</h2>
    
    <c:if test="${not empty message}">
        <div style="background: #f8d7da; color: #721c24; padding: 10px; margin: 10px 0;">
            ${message}
        </div>
    </c:if>
    
    <form action="${pageContext.request.contextPath}/user/joinPost" method="post" enctype="multipart/form-data">
        
        <!-- 아이디 -->
        <div>
            <label>아이디 *</label><br>
            <input type="text" id="userId" name="userId" required>
            <button type="button" onclick="checkUserId()">중복확인</button>
            <div id="userIdFeedback" class="feedback"></div>
        </div>
        
        <!-- 이름 -->
        <div>
            <label>이름 *</label><br>
            <input type="text" id="username" name="username" required>
        </div>
        
        <!-- 이메일 -->
        <div>
            <label>이메일 *</label><br>
            <input type="email" id="email" name="email" required>
            <button type="button" onclick="checkEmailAndSend()">인증발송</button>
            <div id="emailFeedback" class="feedback"></div>
        </div>
        
        <!-- 이메일 인증 -->
        <div id="emailVerifySection" style="display:none;">
            <label>이메일 인증번호</label><br>
            <input type="text" id="emailCode" placeholder="6자리 숫자">
            <button type="button" onclick="verifyEmail()">인증확인</button>
            <div id="emailCodeFeedback" class="feedback"></div>
        </div>
        
        <!-- 전화번호 -->
        <div>
            <label>전화번호 *</label><br>
            <input type="tel" id="phone" name="phone" placeholder="01012345678" required>
            <button type="button" onclick="sendSMS()">SMS발송</button>
            <div id="phoneFeedback" class="feedback"></div>
        </div>
        
        <!-- SMS 인증 -->
        <div id="smsVerifySection" style="display:none;">
            <label>SMS 인증번호</label><br>
            <input type="text" id="smsCode" placeholder="6자리 숫자">
            <button type="button" onclick="verifySMS()">인증확인</button>
            <div id="smsFeedback" class="feedback"></div>
        </div>
        
        <!-- 비밀번호 -->
        <div>
            <label>비밀번호 *</label><br>
            <input type="password" id="password" name="password" required>
        </div>
        
        <!-- 비밀번호 확인 -->
        <div>
            <label>비밀번호 확인 *</label><br>
            <input type="password" id="passwordConfirm" required>
            <div id="passwordFeedback" class="feedback"></div>
        </div>
        
        <!-- 생년월일 -->
        <div>
            <label>생년월일</label><br>
            <input type="date" id="birthDate" name="birthDate">
        </div>
        
        <!-- 성별 -->
        <div>
            <label>성별</label><br>
            <input type="radio" name="gender" value="M" id="genderM"> 남성
            <input type="radio" name="gender" value="F" id="genderF"> 여성
        </div>
        
        <!-- 약관 동의 -->
        <div>
            <input type="checkbox" id="agreeTerms" required> 이용약관 및 개인정보처리방침에 동의합니다.
        </div>
        
        <!-- 테스트용 버튼 -->
        <c:if test="${param.debug == 'true'}">
            <div>
                <button type="button" onclick="skipValidation()">테스트용: 검증 건너뛰기</button>
            </div>
        </c:if>
        
        <div>
            <button type="submit" id="submitBtn" disabled>회원가입</button>
        </div>
        
    </form>

    <script>
        console.log('=== 최소 버전 스크립트 로드 ===');
        
        // 전역 변수
        let userIdChecked = false;
        let emailChecked = false;
        let emailVerified = false;
        let phoneVerified = false;
        
        // 아이디 중복확인
        function checkUserId() {
            console.log('checkUserId 호출');
            
            const userId = document.getElementById('userId').value.trim();
            console.log('아이디:', userId);
            
            if (!userId) {
                alert('아이디를 입력하세요');
                return;
            }
            
            fetch('${pageContext.request.contextPath}/user/checkUserId?userId=' + encodeURIComponent(userId))
                .then(response => {
                    console.log('응답 상태:', response.status);
                    return response.json();
                })
                .then(data => {
                    console.log('받은 데이터:', data);
                    const feedback = document.getElementById('userIdFeedback');
                    if (data.available) {
                        feedback.className = 'feedback valid';
                        feedback.textContent = data.message;
                        userIdChecked = true;
                    } else {
                        feedback.className = 'feedback invalid';
                        feedback.textContent = data.message;
                        userIdChecked = false;
                    }
                    updateSubmitButton();
                })
                .catch(error => {
                    console.error('에러:', error);
                    alert('오류 발생: ' + error.message);
                });
        }
        
        // 이메일 중복확인 + 발송
        function checkEmailAndSend() {
            console.log('checkEmailAndSend 호출');
            
            const email = document.getElementById('email').value.trim();
            console.log('이메일:', email);
            
            if (!email) {
                alert('이메일을 입력하세요');
                return;
            }
            
            // 중복확인
            fetch('${pageContext.request.contextPath}/user/checkEmail?email=' + encodeURIComponent(email))
                .then(response => response.json())
                .then(data => {
                    console.log('이메일 중복확인:', data);
                    if (data.available) {
                        const feedback = document.getElementById('emailFeedback');
                        feedback.className = 'feedback valid';
                        feedback.textContent = '중복확인 완료. 인증번호 발송 중...';
                        emailChecked = true;
                        
                        // 인증발송
                        return fetch('${pageContext.request.contextPath}/user/sendEmail', {
                            method: 'POST',
                            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                            body: 'email=' + encodeURIComponent(email)
                        });
                    } else {
                        const feedback = document.getElementById('emailFeedback');
                        feedback.className = 'feedback invalid';
                        feedback.textContent = data.message;
                        emailChecked = false;
                        throw new Error('이메일 중복');
                    }
                })
                .then(response => response.json())
                .then(data => {
                    console.log('이메일 발송 결과:', data);
                    if (data.success) {
                        document.getElementById('emailVerifySection').style.display = 'block';
                        const feedback = document.getElementById('emailFeedback');
                        feedback.className = 'feedback valid';
                        feedback.textContent = '인증번호가 발송되었습니다.';
                    } else {
                        const feedback = document.getElementById('emailFeedback');
                        feedback.className = 'feedback invalid';
                        feedback.textContent = '발송 실패: ' + data.message;
                    }
                    updateSubmitButton();
                })
                .catch(error => {
                    console.error('이메일 에러:', error);
                    if (error.message !== '이메일 중복') {
                        alert('오류 발생');
                    }
                });
        }
        
        // 이메일 인증확인
        function verifyEmail() {
            console.log('verifyEmail 호출');
            
            const email = document.getElementById('email').value.trim();
            const code = document.getElementById('emailCode').value.trim();
            
            if (!code) {
                alert('인증번호를 입력하세요');
                return;
            }
            
            fetch('${pageContext.request.contextPath}/user/verifyEmail', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: 'email=' + encodeURIComponent(email) + '&code=' + encodeURIComponent(code)
            })
                .then(response => response.json())
                .then(data => {
                    console.log('이메일 인증 결과:', data);
                    const feedback = document.getElementById('emailCodeFeedback');
                    if (data.success) {
                        feedback.className = 'feedback valid';
                        feedback.textContent = data.message;
                        emailVerified = true;
                        document.getElementById('emailVerifySection').style.display = 'none';
                    } else {
                        feedback.className = 'feedback invalid';
                        feedback.textContent = data.message;
                        emailVerified = false;
                    }
                    updateSubmitButton();
                })
                .catch(error => {
                    console.error('인증 에러:', error);
                    alert('인증 오류');
                });
        }
        
        // SMS 발송
        function sendSMS() {
            console.log('sendSMS 호출');
            
            const phone = document.getElementById('phone').value.trim();
            if (!phone) {
                alert('전화번호를 입력하세요');
                return;
            }
            
            fetch('${pageContext.request.contextPath}/user/sendSMS', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: 'phone=' + encodeURIComponent(phone)
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        document.getElementById('smsVerifySection').style.display = 'block';
                        document.getElementById('phoneFeedback').className = 'feedback valid';
                        document.getElementById('phoneFeedback').textContent = data.message;
                    } else {
                        document.getElementById('phoneFeedback').className = 'feedback invalid';
                        document.getElementById('phoneFeedback').textContent = data.message;
                    }
                })
                .catch(error => {
                    console.error('SMS 에러:', error);
                    alert('SMS 오류');
                });
        }
        
        // SMS 인증
        function verifySMS() {
            console.log('verifySMS 호출');
            
            const phone = document.getElementById('phone').value.trim();
            const code = document.getElementById('smsCode').value.trim();
            
            if (!code) {
                alert('인증번호를 입력하세요');
                return;
            }
            
            fetch('${pageContext.request.contextPath}/user/verifySMS', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: 'phone=' + encodeURIComponent(phone) + '&code=' + encodeURIComponent(code)
            })
                .then(response => response.json())
                .then(data => {
                    const feedback = document.getElementById('smsFeedback');
                    if (data.success) {
                        feedback.className = 'feedback valid';
                        feedback.textContent = data.message;
                        phoneVerified = true;
                        document.getElementById('smsVerifySection').style.display = 'none';
                    } else {
                        feedback.className = 'feedback invalid';
                        feedback.textContent = data.message;
                        phoneVerified = false;
                    }
                    updateSubmitButton();
                })
                .catch(error => {
                    console.error('SMS 인증 에러:', error);
                    alert('SMS 인증 오류');
                });
        }
        
        // 테스트용
        function skipValidation() {
            userIdChecked = true;
            emailChecked = true;
            emailVerified = true;
            phoneVerified = true;
            
            document.getElementById('userIdFeedback').className = 'feedback valid';
            document.getElementById('userIdFeedback').textContent = '테스트: 아이디 확인';
            
            document.getElementById('emailFeedback').className = 'feedback valid';
            document.getElementById('emailFeedback').textContent = '테스트: 이메일 확인';
            
            document.getElementById('phoneFeedback').className = 'feedback valid';
            document.getElementById('phoneFeedback').textContent = '테스트: 전화번호 확인';
            
            updateSubmitButton();
        }
        
        // 비밀번호 확인
        function checkPasswordMatch() {
            const password = document.getElementById('password').value;
            const confirm = document.getElementById('passwordConfirm').value;
            const feedback = document.getElementById('passwordFeedback');
            
            if (!confirm) {
                feedback.textContent = '';
                feedback.className = 'feedback';
                updateSubmitButton();
                return;
            }
            
            if (password === confirm) {
                feedback.className = 'feedback valid';
                feedback.textContent = '비밀번호 일치';
            } else {
                feedback.className = 'feedback invalid';
                feedback.textContent = '비밀번호 불일치';
            }
            updateSubmitButton();
        }
        
        // 제출 버튼 활성화
        function updateSubmitButton() {
            const userId = document.getElementById('userId').value.trim();
            const username = document.getElementById('username').value.trim();
            const email = document.getElementById('email').value.trim();
            const phone = document.getElementById('phone').value.trim();
            const password = document.getElementById('password').value;
            const confirm = document.getElementById('passwordConfirm').value;
            const terms = document.getElementById('agreeTerms').checked;
            
            const allValid = userIdChecked && 
                           emailChecked && 
                           emailVerified &&
                           phoneVerified && 
                           userId && 
                           username && 
                           email && 
                           phone && 
                           password && 
                           confirm && 
                           (password === confirm) && 
                           terms;
            
            document.getElementById('submitBtn').disabled = !allValid;
            
            console.log('검증:', {
                userIdChecked,
                emailChecked,
                emailVerified,
                phoneVerified,
                allValid
            });
        }
        
        // 이벤트 리스너
        document.getElementById('password').addEventListener('input', checkPasswordMatch);
        document.getElementById('passwordConfirm').addEventListener('input', checkPasswordMatch);
        document.getElementById('agreeTerms').addEventListener('change', updateSubmitButton);
        
        document.getElementById('userId').addEventListener('input', function() {
            if (userIdChecked) {
                userIdChecked = false;
                document.getElementById('userIdFeedback').textContent = '';
                updateSubmitButton();
            }
        });
        
        document.getElementById('email').addEventListener('input', function() {
            if (emailChecked) {
                emailChecked = false;
                emailVerified = false;
                document.getElementById('emailFeedback').textContent = '';
                document.getElementById('emailVerifySection').style.display = 'none';
                updateSubmitButton();
            }
        });
        
        document.getElementById('username').addEventListener('input', updateSubmitButton);
        document.getElementById('phone').addEventListener('input', updateSubmitButton);
        
        console.log('모든 스크립트 로드 완료');
    </script>
</body>
</html>