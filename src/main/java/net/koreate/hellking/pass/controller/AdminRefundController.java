package net.koreate.hellking.pass.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.koreate.hellking.pass.service.PassService;
import net.koreate.hellking.pass.vo.RefundVO;
import net.koreate.hellking.user.service.UserService;

@Controller
@RequestMapping("/pass/admin/refund/")
public class AdminRefundController {
    
    // 생성자 추가 (이 부분을 추가하세요)
    public AdminRefundController() {
        System.out.println("=== AdminRefundController 생성됨 ===");
    }
    
    @Autowired
    private PassService passService;
    
    @Autowired
    private UserService userService;
    
    // 환불 신청 목록 조회
    @GetMapping("list")
    public String refundList(Model model, HttpSession session) {
        System.out.println("=== AdminRefundController.refundList() 시작 ===");
        
        // 권한 체크 활성화
        if (!userService.isAdmin(session)) {
            System.out.println("관리자 권한 없음 - 메인으로 리다이렉트");
            return "redirect:/";
        }
        
        try {
            // 모든 환불 신청 조회
            List<RefundVO> refunds = passService.getAllRefunds();
            System.out.println("조회된 환불 신청 수: " + (refunds != null ? refunds.size() : 0));
            
            // 상태별 통계 계산
            long requestedCount = 0, approvedCount = 0, rejectedCount = 0, completedCount = 0;
            
            if (refunds != null) {
                for (RefundVO refund : refunds) {
                    switch (refund.getStatus()) {
                        case "REQUESTED": requestedCount++; break;
                        case "APPROVED": approvedCount++; break;
                        case "REJECTED": rejectedCount++; break;
                        case "COMPLETED": completedCount++; break;
                    }
                }
            }
            
            System.out.println("환불 통계:");
            System.out.println("- 신청 대기: " + requestedCount);
            System.out.println("- 승인됨: " + approvedCount);
            System.out.println("- 거절됨: " + rejectedCount);
            System.out.println("- 완료됨: " + completedCount);
            
            // 모델에 데이터 추가
            model.addAttribute("refunds", refunds);
            model.addAttribute("requestedCount", requestedCount);
            model.addAttribute("approvedCount", approvedCount);
            model.addAttribute("rejectedCount", rejectedCount);
            model.addAttribute("completedCount", completedCount);
            
            System.out.println("pass/admin_refund_list JSP로 리턴");
            return "pass/admin_refund_list";
            
        } catch (Exception e) {
            System.out.println("환불 목록 조회 중 오류 발생:");
            System.out.println("오류 메시지: " + e.getMessage());
            e.printStackTrace();
            
            model.addAttribute("error", "환불 목록을 불러오는 중 오류가 발생했습니다.");
            model.addAttribute("refunds", null);
            model.addAttribute("requestedCount", 0);
            model.addAttribute("approvedCount", 0);
            model.addAttribute("rejectedCount", 0);
            model.addAttribute("completedCount", 0);
            
            return "pass/admin_refund_list";
        }
    }
    
    // 환불 승인
    @PostMapping("approve")
    @ResponseBody
    public Map<String, Object> approveRefund(@RequestParam Long refundNum, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        System.out.println("=== 환불 승인 처리 시작 ===");
        System.out.println("처리할 refundNum: " + refundNum);
        
        try {
            // 관리자 권한 체크
            if (!userService.isAdmin(session)) {
                result.put("success", false);
                result.put("message", "관리자 권한이 필요합니다.");
                return result;
            }
            // if (!userService.isAdmin(session)) {
            //     result.put("success", false);
            //     result.put("message", "관리자 권한이 필요합니다.");
            //     return result;
            // }
            
            if (refundNum == null || refundNum <= 0) {
                result.put("success", false);
                result.put("message", "유효하지 않은 환불 번호입니다.");
                return result;
            }
            
            boolean success = passService.processRefund(refundNum, "APPROVED", null);
            
            if (success) {
                result.put("success", true);
                result.put("message", "환불이 승인되었습니다.");
                System.out.println("환불 승인 완료: refundNum=" + refundNum);
            } else {
                result.put("success", false);
                result.put("message", "환불 승인에 실패했습니다.");
                System.out.println("환불 승인 실패: refundNum=" + refundNum);
            }
            
        } catch (Exception e) {
            System.out.println("환불 승인 중 오류 발생:");
            System.out.println("오류 메시지: " + e.getMessage());
            e.printStackTrace();
            
            result.put("success", false);
            result.put("message", "오류가 발생했습니다: " + e.getMessage());
        }
        
        return result;
    }
    
    // 환불 거절
    @PostMapping("reject")
    @ResponseBody
    public Map<String, Object> rejectRefund(@RequestParam Long refundNum, 
                                          @RequestParam String rejectReason, 
                                          HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        System.out.println("=== 환불 거절 처리 시작 ===");
        System.out.println("처리할 refundNum: " + refundNum);
        System.out.println("거절 사유: " + rejectReason);
        
        try {
            // TODO: 관리자 권한 체크
            // if (!userService.isAdmin(session)) {
            //     result.put("success", false);
            //     result.put("message", "관리자 권한이 필요합니다.");
            //     return result;
            // }
            
            // 입력값 검증
            if (refundNum == null || refundNum <= 0) {
                result.put("success", false);
                result.put("message", "유효하지 않은 환불 번호입니다.");
                return result;
            }
            
            if (rejectReason == null || rejectReason.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "거절 사유를 입력해주세요.");
                return result;
            }
            
            if (rejectReason.trim().length() < 10) {
                result.put("success", false);
                result.put("message", "거절 사유를 10글자 이상 입력해주세요.");
                return result;
            }
            
            boolean success = passService.processRefund(refundNum, "REJECTED", rejectReason.trim());
            
            if (success) {
                result.put("success", true);
                result.put("message", "환불이 거절되었습니다.");
                System.out.println("환불 거절 완료: refundNum=" + refundNum);
            } else {
                result.put("success", false);
                result.put("message", "환불 거절 처리에 실패했습니다.");
                System.out.println("환불 거절 실패: refundNum=" + refundNum);
            }
            
        } catch (Exception e) {
            System.out.println("환불 거절 중 오류 발생:");
            System.out.println("오류 메시지: " + e.getMessage());
            e.printStackTrace();
            
            result.put("success", false);
            result.put("message", "오류가 발생했습니다: " + e.getMessage());
        }
        
        return result;
    }
    
    // 환불 완료 처리 (승인 후 실제 환불 완료)
    @PostMapping("complete")
    @ResponseBody
    public Map<String, Object> completeRefund(@RequestParam Long refundNum, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        System.out.println("=== 환불 완료 처리 시작 ===");
        System.out.println("처리할 refundNum: " + refundNum);
        
        try {
            // TODO: 관리자 권한 체크
            
            if (refundNum == null || refundNum <= 0) {
                result.put("success", false);
                result.put("message", "유효하지 않은 환불 번호입니다.");
                return result;
            }
            
            boolean success = passService.processRefund(refundNum, "COMPLETED", null);
            
            if (success) {
                result.put("success", true);
                result.put("message", "환불이 완료 처리되었습니다.");
                System.out.println("환불 완료 처리 성공: refundNum=" + refundNum);
            } else {
                result.put("success", false);
                result.put("message", "환불 완료 처리에 실패했습니다.");
                System.out.println("환불 완료 처리 실패: refundNum=" + refundNum);
            }
            
        } catch (Exception e) {
            System.out.println("환불 완료 처리 중 오류 발생:");
            System.out.println("오류 메시지: " + e.getMessage());
            e.printStackTrace();
            
            result.put("success", false);
            result.put("message", "오류가 발생했습니다: " + e.getMessage());
        }
        
        return result;
    }
}