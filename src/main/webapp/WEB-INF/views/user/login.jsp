<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>로그인 - 헬킹 피트니스</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        :root {
            --bg-cream: #F4ECDC;
            --brand: #FF6A00;
            --ink: #0F172A;
        }
        body { background: var(--bg-cream); }
        .login-container { 
            max-width: 400px; 
            margin: 100px auto; 
            background: #fff; 
            padding: 40px;
            border-radius: 20px;
            box-shadow: 0 10px 30px rgba(15,23,42,0.1);
        }
        .brand-logo {
            text-align: center;
            font-size: 28px;
            font-weight: 900;
            color: var(--brand);
            margin-bottom: 30px;
        }
        .btn-primary {
            background: var(--brand);
            border: none;
            font-weight: 700;
            padding: 12px;
            border-radius: 12px;
        }
        .form-control {
            border-radius: 12px;
            padding: 12px 16px;
            border: 2px solid #E7E0D6;
        }
        .form-control:focus {
            border-color: var(--brand);
            box-shadow: 0 0 0 0.2rem rgba(255,106,0,0.1);
        }
    </style>
</head>
<body>
    <jsp:include page="../common/header.jsp" />
    
    <div class="login-container">
        <div class="brand-logo">🏋️ HELLKING</div>
        
        <c:if test="${not empty message}">
            <div class="alert alert-danger" role="alert">${message}</div>
        </c:if>
        
        <form action="${pageContext.request.contextPath}/user/login" method="post">
            <div class="mb-3">
                <label for="userId" class="form-label">아이디</label>
                <input type="text" class="form-control" id="userId" name="userId" required>
            </div>
            <div class="mb-3">
                <label for="password" class="form-label">비밀번호</label>
                <input type="password" class="form-control" id="password" name="password" required>
            </div>
            
            <!-- Remember me 체크박스 추가 -->
            <div class="mb-3 form-check">
                <input type="checkbox" class="form-check-input" name="rememberme" id="rememberme">
                <label class="form-check-label" for="rememberme">로그인 정보 저장</label>
            </div>
            
            <button type="submit" class="btn btn-primary w-100 mb-3">로그인</button>
            
            <div class="text-center">
                <a href="${pageContext.request.contextPath}/user/join" class="text-decoration-none me-3">회원가입</a>
                <a href="${pageContext.request.contextPath}/user/findId" class="text-decoration-none me-3">아이디 찾기</a>
                <a href="${pageContext.request.contextPath}/user/findPassword" class="text-decoration-none">비밀번호 찾기</a>
            </div>
        </form>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>