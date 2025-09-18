package net.koreate.hellking.user.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.extern.slf4j.Slf4j;
import net.koreate.hellking.user.service.UserService;
import net.koreate.hellking.user.vo.UserVO;

/**
 * 통합 UserController - 두 버전의 기능을 모두 지원
 */
@Controller
@RequestMapping("/user/")
@Slf4j
public class UserController {
    
    @Autowired
    private UserService userService;
    
    // === 로그인 관련 ===
    
    @GetMapping("login")
    public String login() {
        return "user/login";
    }
    
    /**
     * 통합 로그인 처리 (두 버전 모두 호환)
     */
    @PostMapping("login")
    public String loginPost(String userId, String user_id, String password, 
                           HttpSession session, RedirectAttributes rttr) {
        try {
            // userId와 user_id 둘 다 지원
            String loginId = userId != null ? userId : user_id;
            
            if (loginId == null || loginId.trim().isEmpty()) {
                throw new Exception("아이디를 입력해주세요.");
            }
            
            UserVO user = userService.login(loginId, password, session);
            
            log.info("로그인 성공: {} ({})", user.getUsername(), user.getUserId());
            return "redirect:/";
        } catch (Exception e) {
            log.error("로그인 실패: {}", e.getMessage());
            rttr.addFlashAttribute("message", e.getMessage());
            return "redirect:/user/login";
        }
    }
    
    @GetMapping("logout")
    public String logout(HttpSession session) {
        userService.logout(session);
        log.info("로그아웃 완료");
        return "redirect:/";
    }
    
    // === 회원가입 관련 ===
    
    @GetMapping("join")
    public String join() {
        return "user/join";
    }
    
    /**
     * 새 버전 회원가입
     */
    @PostMapping("joinPost")
    public String joinPost(UserVO user, MultipartFile profileImage, RedirectAttributes rttr) {
        try {
            log.info("새 버전 회원가입 시도: {}", user.getUserId());
            
            // 프로필 이미지가 있는 경우 처리
            if (profileImage != null && !profileImage.isEmpty()) {
                // 팀원 버전 메서드 활용
                userService.userJoin(user, profileImage);
            } else {
                // 기본 이미지로 처리
                userService.registerUser(user);
            }
            
            rttr.addFlashAttribute("message", "회원가입이 완료되었습니다.");
            return "redirect:/user/login";
        } catch (Exception e) {
            log.error("회원가입 실패: {}", e.getMessage());
            rttr.addFlashAttribute("message", e.getMessage());
            return "redirect:/user/join";
        }
    }
    
    /**
     * 팀원 버전 회원가입 호환
     */
    @PostMapping("joinPostOld")
    public String joinPostOld(UserVO vo, MultipartFile profileImage, RedirectAttributes rttr) {
        try {
            log.info("팀원 버전 회원가입 시도: {}", vo.getUserId());
            String message = userService.userJoin(vo, profileImage);
            rttr.addFlashAttribute("message", "SUCCESS".equals(message) ? "회원가입이 완료되었습니다." : "회원가입에 실패했습니다.");
            return "redirect:/user/login";
        } catch (Exception e) {
            log.error("회원가입 실패: {}", e.getMessage());
            rttr.addFlashAttribute("message", e.getMessage());
            return "redirect:/user/join";
        }
    }
    
    // === 마이페이지 관련 ===
    
    @GetMapping("mypage")
    public String mypage(HttpSession session, Model model) {
        if (!userService.isLoggedIn(session)) {
            return "redirect:/user/login";
        }
        
        Long userNum = userService.getCurrentUserNum(session);
        UserVO user = userService.getUserWithActivePass(userNum);
        model.addAttribute("user", user);
        return "user/mypage";
    }
    
    @GetMapping("edit")
    public String edit(HttpSession session, Model model) {
        if (!userService.isLoggedIn(session)) {
            return "redirect:/user/login";
        }
        
        UserVO user = userService.getCurrentUser(session);
        model.addAttribute("user", user);
        return "user/edit";
    }
    
    @PostMapping("editPost")
    public String editPost(UserVO user, HttpSession session, RedirectAttributes rttr) {
        if (!userService.isLoggedIn(session)) {
            return "redirect:/user/login";
        }
        
        try {
            Long userNum = userService.getCurrentUserNum(session);
            user.setUserNum(userNum);
            
            userService.updateUser(user);
            rttr.addFlashAttribute("message", "회원정보가 수정되었습니다.");
            return "redirect:/user/mypage";
        } catch (Exception e) {
            log.error("정보 수정 실패: {}", e.getMessage());
            rttr.addFlashAttribute("message", "수정 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/user/edit";
        }
    }
    
    // === 비밀번호 변경 ===
    
    @GetMapping("changePassword")
    public String changePassword(HttpSession session) {
        if (!userService.isLoggedIn(session)) {
            return "redirect:/user/login";
        }
        return "user/changePassword";
    }
    
    @PostMapping("changePasswordPost")
    @ResponseBody
    public Map<String, Object> changePasswordPost(String currentPassword, String newPassword, 
                                                 HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (!userService.isLoggedIn(session)) {
                result.put("success", false);
                result.put("message", "로그인이 필요합니다.");
                return result;
            }
            
            Long userNum = userService.getCurrentUserNum(session);
            boolean success = userService.updatePassword(userNum, currentPassword, newPassword);
            
            result.put("success", success);
            result.put("message", success ? "비밀번호가 변경되었습니다." : "비밀번호 변경에 실패했습니다.");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        
        return result;
    }
    
    // === 중복 체크 API ===
    
    @GetMapping("checkUserId")
    @ResponseBody
    public Map<String, Object> checkUserId(String userId) {
        Map<String, Object> result = new HashMap<>();
        boolean available = userService.isUserIdAvailable(userId);
        result.put("available", available);
        result.put("message", available ? "사용 가능한 아이디입니다." : "이미 사용중인 아이디입니다.");
        return result;
    }
    
    @GetMapping("checkEmail")
    @ResponseBody
    public Map<String, Object> checkEmail(String email) {
        Map<String, Object> result = new HashMap<>();
        boolean available = userService.isEmailAvailable(email);
        result.put("available", available);
        result.put("message", available ? "사용 가능한 이메일입니다." : "이미 사용중인 이메일입니다.");
        return result;
    }
    
    @GetMapping("checkPhone")
    @ResponseBody
    public Map<String, Object> checkPhone(String phone) {
        Map<String, Object> result = new HashMap<>();
        boolean available = userService.isPhoneAvailable(phone);
        result.put("available", available);
        result.put("message", available ? "사용 가능한 전화번호입니다." : "이미 사용중인 전화번호입니다.");
        return result;
    }
    
    // === 인증 관련 API ===
    
    @PostMapping("sendSMS")
    @ResponseBody
    public Map<String, Object> sendSMS(String phone) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = userService.sendSMSAuthCode(phone);
            result.put("success", success);
            result.put("message", success ? "인증번호가 발송되었습니다." : "발송에 실패했습니다.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "오류가 발생했습니다: " + e.getMessage());
        }
        
        return result;
    }
    
    @PostMapping("verifySMS")
    @ResponseBody
    public Map<String, Object> verifySMS(String phone, String code) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = userService.verifySMSAuthCode(phone, code);
            result.put("success", success);
            result.put("message", success ? "인증이 완료되었습니다." : "인증번호가 올바르지 않습니다.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "오류가 발생했습니다: " + e.getMessage());
        }
        
        return result;
    }
    
    @PostMapping("sendEmail")
    @ResponseBody
    public Map<String, Object> sendEmail(String email) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = userService.sendEmailAuthCode(email);
            result.put("success", success);
            result.put("message", success ? "인증번호가 발송되었습니다." : "발송에 실패했습니다.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "오류가 발생했습니다: " + e.getMessage());
        }
        
        return result;
    }
    
    @PostMapping("verifyEmail")
    @ResponseBody
    public Map<String, Object> verifyEmail(String email, String code) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = userService.verifyEmailAuthCode(email, code);
            result.put("success", success);
            result.put("message", success ? "이메일 인증이 완료되었습니다." : "인증번호가 올바르지 않습니다.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "오류가 발생했습니다: " + e.getMessage());
        }
        
        return result;
    }
    
    // === 아이디/패스워드 찾기 ===
    
    @GetMapping("findId")
    public String findId() {
        return "user/findId";
    }
    
    @PostMapping("findIdPost")
    @ResponseBody
    public Map<String, Object> findIdPost(String email, String phone) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String userId = userService.findUserId(email, phone);
            if (userId != null) {
                result.put("success", true);
                result.put("userId", userId);
                result.put("message", "아이디를 찾았습니다.");
            } else {
                result.put("success", false);
                result.put("message", "일치하는 회원정보가 없습니다.");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "오류가 발생했습니다: " + e.getMessage());
        }
        
        return result;
    }
    
    @GetMapping("findPassword")
    public String findPassword() {
        return "user/findPassword";
    }
    
    @PostMapping("findPasswordPost")
    @ResponseBody
    public Map<String, Object> findPasswordPost(String userId, String email, String newPassword) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = userService.resetPassword(userId, email, newPassword);
            result.put("success", success);
            result.put("message", success ? "비밀번호가 재설정되었습니다." : "비밀번호 재설정에 실패했습니다.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        
        return result;
    }
    
    // === 회원탈퇴 ===
    
    @GetMapping("withdraw")
    public String withdraw(HttpSession session) {
        if (!userService.isLoggedIn(session)) {
            return "redirect:/user/login";
        }
        return "user/withdraw";
    }
    
    @PostMapping("withdrawPost")
    public String withdrawPost(String password, HttpSession session, RedirectAttributes rttr) {
        try {
            if (!userService.isLoggedIn(session)) {
                return "redirect:/user/login";
            }
            
            Long userNum = userService.getCurrentUserNum(session);
            
            // 실제로는 비밀번호 확인 후 계정 비활성화
            userService.deactivateUser(userNum);
            session.invalidate();
            
            rttr.addFlashAttribute("message", "회원탈퇴가 완료되었습니다.");
            return "redirect:/";
            
        } catch (Exception e) {
            rttr.addFlashAttribute("message", "회원탈퇴 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/user/withdraw";
        }
    }
    
    // === 팀원 버전 호환 메서드들 ===
    
    /**
     * 팀원 버전 호환: 아이디 중복 체크
     * @deprecated checkUserId 사용 권장
     */
    @GetMapping("uidCheck")
    @ResponseBody
    public boolean uidCheck(String user_id) throws Exception {
        UserVO user = userService.getUserById(user_id);
        return user == null;
    }
}