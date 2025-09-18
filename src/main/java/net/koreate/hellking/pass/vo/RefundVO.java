package net.koreate.hellking.pass.vo;

import lombok.Data;
import java.util.Date;

@Data
public class RefundVO {
    private Long refundNum;
    private Long userPassNum;
    private Long refundAmount;
    private String reason;
    private String status;              // REQUESTED, APPROVED, REJECTED, COMPLETED
    private Date requestDate;
    private Date processDate;           // 처리일 (승인/거절/완료 시점)
    private String rejectReason;        // 거절 사유
    
    // 조인용 필드들
    private Long passNum;
    private String passName;
    private String username;
    private Long originalPrice;         // 원래 결제 금액
    
    // 표시용 필드들
    private String statusText;          // 상태 텍스트
    private String formattedRefundAmount; // 포맷된 환불 금액
}