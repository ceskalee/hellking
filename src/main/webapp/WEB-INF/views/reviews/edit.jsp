<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>리뷰 수정 - 헬킹 피트니스</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        :root {
            --brand: #FF6A00;
            --bg-cream: #F4ECDC;
        }
        body { background: var(--bg-cream); }
        .rating-input {
            font-size: 2rem;
            color: #ddd;
            cursor: pointer;
            transition: color 0.2s;
        }
        .rating-input.active, .rating-input:hover {
            color: #ffc107;
        }
        .btn-primary {
            background: var(--brand);
            border-color: var(--brand);
        }
        .btn-primary:hover {
            background: #e55a00;
            border-color: #e55a00;
        }
    </style>
</head>
<body>
    <jsp:include page="../common/header.jsp" />
    
    <div class="container mt-4 mb-5">
        <div class="row justify-content-center">
            <div class="col-lg-8">
                <div class="card shadow">
                    <div class="card-header bg-white">
                        <h4 class="mb-0"><i class="fas fa-edit me-2"></i>리뷰 수정</h4>
                    </div>
                    <div class="card-body">
                        <!-- 에러 메시지 표시 -->
                        <c:if test="${not empty message}">
                            <div class="alert alert-danger" role="alert">${message}</div>
                        </c:if>
                        
                        <form action="${pageContext.request.contextPath}/reviews/edit" method="post" id="editForm">
                            <!-- 숨겨진 필드들 -->
                            <input type="hidden" name="reviewNum" value="${review.reviewNum}">
                            <input type="hidden" name="userNum" value="${review.userNum}">
                            <input type="hidden" name="chainNum" value="${review.chainNum}">
                            
                            <!-- 가맹점 정보 (읽기 전용) -->
                            <div class="mb-4">
                                <label class="form-label fw-bold">가맹점</label>
                                <div class="p-3 bg-light rounded border">
                                    <div class="d-flex align-items-center">
                                        <img src="${pageContext.request.contextPath}/resources/images/chains/default-chain.jpg" 
                                             class="rounded me-3" width="60" height="60" alt="${review.chainName}"
                                             onerror="this.src='${pageContext.request.contextPath}/resources/images/default-chain.jpg'">
                                        <div>
                                            <h6 class="mb-1">${review.chainName}</h6>
                                            <p class="text-muted mb-0 small">${review.chainAddress}</p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            
                            <!-- 평점 입력 -->
                            <div class="mb-4">
                                <label class="form-label fw-bold">평점 <span class="text-danger">*</span></label>
                                <div class="rating-container mb-2">
                                    <c:forEach begin="1" end="5" var="star">
                                        <i class="fas fa-star rating-input" data-rating="${star}"></i>
                                    </c:forEach>
                                </div>
                                <input type="hidden" name="rating" id="ratingValue" value="${review.rating}" required>
                                <div class="form-text text-muted">
                                    <i class="fas fa-star text-warning me-1"></i>
                                    현재 평점: ${review.formattedRating}점
                                </div>
                            </div>
                            
                            <!-- 제목 입력 -->
                            <div class="mb-4">
                                <label for="title" class="form-label fw-bold">제목 <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="title" name="title" 
                                       value="${review.title}" placeholder="리뷰 제목을 입력하세요" 
                                       maxlength="100" required>
                                <div class="form-text text-end">
                                    <span id="titleCount">0</span> / 100자
                                </div>
                            </div>
                            
                            <!-- 내용 입력 -->
                            <div class="mb-4">
                                <label for="content" class="form-label fw-bold">내용 <span class="text-danger">*</span></label>
                                <textarea class="form-control" id="content" name="content" rows="8" 
                                          placeholder="가맹점 이용 경험을 자세히 작성해주세요." 
                                          maxlength="2000" required>${review.content}</textarea>
                                <div class="form-text text-end">
                                    <span id="contentCount">0</span> / 2000자
                                </div>
                            </div>
                            
                            <!-- 수정 팁 -->
                            <div class="alert alert-light border">
                                <h6 class="text-primary"><i class="fas fa-lightbulb me-2"></i>리뷰 수정 안내</h6>
                                <ul class="mb-0 small">
                                    <li>수정된 내용은 즉시 반영됩니다</li>
                                    <li>가맹점은 변경할 수 없습니다</li>
                                    <li>평점, 제목, 내용을 수정할 수 있습니다</li>
                                    <li>욕설이나 비방은 삼가해주세요</li>
                                </ul>
                            </div>
                            
                            <div class="d-flex gap-2">
                                <button type="submit" class="btn btn-primary" id="submitBtn">
                                    <i class="fas fa-save me-1"></i>수정 완료
                                </button>
                                <a href="${pageContext.request.contextPath}/reviews/detail/${review.reviewNum}" 
                                   class="btn btn-secondary">
                                    <i class="fas fa-arrow-left me-1"></i>취소
                                </a>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    
    <script>
        // 전역 변수
        let selectedRating = ${review.rating};
        
        // 페이지 로드 시 초기화
        document.addEventListener('DOMContentLoaded', function() {
            console.log('리뷰 수정 페이지 로드됨');
            console.log('기존 평점:', selectedRating);
            
            // 기존 평점 표시
            updateStarDisplay(selectedRating);
            document.getElementById('ratingValue').value = selectedRating;
            
            // 글자수 초기화
            updateCharacterCounts();
        });
        
        // 평점 선택 기능
        function initRatingSystem() {
            const stars = document.querySelectorAll('.rating-input');
            
            stars.forEach((star, index) => {
                // 클릭 이벤트
                star.addEventListener('click', function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    
                    selectedRating = parseInt(this.dataset.rating);
                    document.getElementById('ratingValue').value = selectedRating;
                    
                    console.log('평점 변경됨:', selectedRating);
                    updateStarDisplay(selectedRating);
                });
                
                // 마우스 오버
                star.addEventListener('mouseenter', function() {
                    const hoverRating = parseInt(this.dataset.rating);
                    updateStarDisplay(hoverRating);
                });
                
                // 터치 이벤트 (모바일)
                star.addEventListener('touchend', function(e) {
                    e.preventDefault();
                    this.click();
                });
            });
        }
        
        // 평점 시스템 초기화
        initRatingSystem();
        
        // 마우스가 평점 영역을 벗어날 때
        document.querySelector('.rating-container').addEventListener('mouseleave', function() {
            updateStarDisplay(selectedRating);
        });
        
        // 별표 표시 업데이트 함수
        function updateStarDisplay(rating) {
            document.querySelectorAll('.rating-input').forEach((star, index) => {
                if (index < rating) {
                    star.style.color = '#ffc107';
                    star.classList.add('active');
                } else {
                    star.style.color = '#ddd';
                    star.classList.remove('active');
                }
            });
        }
        
        // 글자수 카운트 초기화
        function updateCharacterCounts() {
            const title = document.getElementById('title');
            const content = document.getElementById('content');
            
            document.getElementById('titleCount').textContent = title.value.length;
            document.getElementById('contentCount').textContent = content.value.length;
        }
        
        // 제목 글자수 카운트
        document.getElementById('title').addEventListener('input', function() {
            const count = this.value.length;
            document.getElementById('titleCount').textContent = count;
            
            if (count > 90) {
                document.getElementById('titleCount').style.color = '#dc3545';
            } else {
                document.getElementById('titleCount').style.color = '#6c757d';
            }
        });
        
        // 내용 글자수 카운트
        document.getElementById('content').addEventListener('input', function() {
            const count = this.value.length;
            document.getElementById('contentCount').textContent = count;
            
            if (count > 1900) {
                document.getElementById('contentCount').style.color = '#dc3545';
            } else {
                document.getElementById('contentCount').style.color = '#6c757d';
            }
        });
        
        // 폼 제출 전 검증
        document.getElementById('editForm').addEventListener('submit', function(e) {
            console.log('리뷰 수정 폼 제출 시도');
            
            // 평점 확인
            const ratingValue = document.getElementById('ratingValue').value;
            if (!ratingValue || ratingValue < 1 || ratingValue > 5) {
                e.preventDefault();
                alert('평점을 선택해주세요 (1-5점).');
                return;
            }
            
            // 제목 확인
            const title = document.getElementById('title').value.trim();
            if (!title) {
                e.preventDefault();
                alert('제목을 입력해주세요.');
                document.getElementById('title').focus();
                return;
            }
            
            if (title.length > 100) {
                e.preventDefault();
                alert('제목은 100자 이내로 입력해주세요.');
                document.getElementById('title').focus();
                return;
            }
            
            // 내용 확인
            const content = document.getElementById('content').value.trim();
            if (content.length < 10) {
                e.preventDefault();
                alert('리뷰 내용을 10글자 이상 입력해주세요.');
                document.getElementById('content').focus();
                return;
            }
            
            if (content.length > 2000) {
                e.preventDefault();
                alert('리뷰 내용은 2000자 이내로 입력해주세요.');
                document.getElementById('content').focus();
                return;
            }
            
            // 최종 확인
            if (!confirm('리뷰를 수정하시겠습니까?')) {
                e.preventDefault();
                return;
            }
            
            console.log('리뷰 수정 폼 제출 승인');
            
            // 제출 버튼 비활성화 (중복 제출 방지)
            const submitBtn = document.getElementById('submitBtn');
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-1"></i>수정 중...';
        });
    </script>
</body>
</html>