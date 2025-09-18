package net.koreate.hellking.user.service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.koreate.hellking.common.utils.FileUtils;
import net.koreate.hellking.user.dao.UserDAO;
import net.koreate.hellking.user.vo.UserVO;

// CoolSMS 관련 import 추가
import net.nurigo.sdk.message.service.DefaultMessageService;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;


/**
 * 통합 UserService - 두 버전의 기능을 모두 지원 + CoolSMS 실제 발송
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class UserService {
    
    private final UserDAO userDAO;
    private final BCryptPasswordEncoder passwordEncoder;
    private final String uploadPath;
    private final ServletContext servletContext;
    
    // 완성된 실제 저장 폴더 경로
    private String realPath;
    
    // CoolSMS 서비스 주입
    @Autowired
    private DefaultMessageService messageService;
    
    // Properties에서 발신번호 주입
    @Value("${sms.from.number}")
    private String fromNumber;
    
    // SMS 인증 코드 임시 저장소 (실제로는 Redis 권장)
    private Map<String, String> smsAuthCodes = new HashMap<>();
    private Map<String, Long> smsAuthTimes = new HashMap<>();
    
    @PostConstruct
    public void initPath() {
        realPath = servletContext.getRealPath(File.separator + uploadPath);
        File file = new File(realPath);
        if (!file.exists()) {
            file.mkdirs();
            log.info("경로 생성 완료: {}", realPath);
        }
        log.info("UserService 초기화 완료 - 발신번호: {}", fromNumber);
    }
    
    // === 새 버전 핵심 메서드 ===
    
    public Long getCurrentUserNum(HttpSession session) {
        // 새 버전 우선 확인
        Long userNum = (Long) session.getAttribute("userNum");
        if (userNum != null) {
            return userNum;
        }
        
        // 팀원 버전 호환 - userInfo에서 userNum 추출
        UserVO userInfo = (UserVO) session.getAttribute("userInfo");
        if (userInfo != null && userInfo.getUserNum() != null) {
            return userInfo.getUserNum();
        }
        
        return null;
    }
    
    public UserVO getCurrentUser(HttpSession session) {
        Long userNum = getCurrentUserNum(session);
        if (userNum != null) {
            return userDAO.selectByUserNum(userNum);
        }
        
        // 팀원 버전 호환
        UserVO userInfo = (UserVO) session.getAttribute("userInfo");
        if (userInfo != null) {
            return userInfo;
        }
        
        return null;
    }
    
    public boolean isLoggedIn(HttpSession session) {
        return getCurrentUserNum(session) != null || session.getAttribute("userInfo") != null;
    }
    
    public boolean isAdmin(HttpSession session) {
        UserVO user = getCurrentUser(session);
        if (user == null) {
            log.debug("관리자 체크: 로그인되지 않은 사용자");
            return false;
        }
        
        // 관리자 계정 목록
        String[] adminUsers = {"admin", "aqunasl22"};
        
        for (String adminId : adminUsers) {
            if (adminId.equals(user.getUserId())) {
                log.debug("관리자 인증 성공: {}", user.getUserId());
                return true;
            }
        }
        
        log.debug("관리자 권한 없음: {}", user.getUserId());
        return false;
    }
    
    // === 회원가입 (통합 버전) ===
    
    public boolean registerUser(UserVO user) throws Exception {
        // 중복 체크
        if (userDAO.checkDuplicateUserId(user.getUserId()) > 0) {
            throw new Exception("이미 사용중인 아이디입니다.");
        }
        if (userDAO.checkDuplicateEmail(user.getEmail()) > 0) {
            throw new Exception("이미 사용중인 이메일입니다.");
        }
        if (userDAO.checkDuplicatePhone(user.getPhone()) > 0) {
            throw new Exception("이미 사용중인 전화번호입니다.");
        }
        
        // 비밀번호 암호화
        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);
        
        // 기본 프로필 이미지 설정
        if (user.getProfileImage() == null || user.getProfileImage().trim().isEmpty()) {
            user.setProfileImage("avatar1.png");
        }
        
        return userDAO.insertUser(user) > 0;
    }
    
    /**
     * 팀원 버전 호환 회원가입
     */
    public String userJoin(UserVO vo, MultipartFile profileImage) throws Exception {
        // 프로필 이미지 업로드 처리
        vo.setProfileImage("");
        if (profileImage != null && !profileImage.isEmpty()) {
            String profileImg = FileUtils.uploadFile(realPath, profileImage);
            log.info("업로드된 프로필 이미지: {}", profileImg);
            vo.setProfileImage(profileImg);
        }
        
        // 이메일이 없으면 userId를 이메일로 사용 (팀원 버전 호환)
        if (vo.getEmail() == null || vo.getEmail().trim().isEmpty()) {
            vo.setEmail(vo.getUserId());
        }
        
        // BCrypt 암호화 적용
        if (vo.getPassword() != null) {
            vo.setPassword(passwordEncoder.encode(vo.getPassword()));
        }
        
        log.info("회원가입 데이터: {}", vo);
        int result = userDAO.insertUser(vo);
        
        return result == 1 ? "SUCCESS" : "FAILED";
    }
    
    // === 로그인 (통합 버전) ===
    
    public UserVO login(String userId, String password, HttpSession session) throws Exception {
        UserVO user = userDAO.selectByUserId(userId);
        
        if (user == null) {
            // 팀원 버전 호환: 평문 비밀번호도 확인
            user = userDAO.login(userId, password);
            if (user == null) {
                throw new Exception("존재하지 않는 아이디입니다.");
            }
            
            // 평문 비밀번호를 BCrypt로 업그레이드
            if (!password.startsWith("$2a$")) {
                String encryptedPassword = passwordEncoder.encode(password);
                userDAO.updatePassword(user.getUserNum(), encryptedPassword);
                log.info("사용자 {}의 비밀번호를 BCrypt로 업그레이드함", userId);
            }
        } else {
            // BCrypt 비밀번호 검증
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new Exception("비밀번호가 올바르지 않습니다.");
            }
        }
        
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new Exception("비활성화된 계정입니다.");
        }
        
        // 디버깅 로그
        log.debug("=== 로그인 성공 ===");
        log.debug("UserNum: {}", user.getUserNum());
        log.debug("UserId: {}", user.getUserId());
        log.debug("Username: {}", user.getUsername());
        
        // 양쪽 버전 모두 호환되도록 세션 설정
        if (user.getUserNum() != null) {
            session.setAttribute("userNum", user.getUserNum());
            log.debug("✅ userNum 세션 저장: {}", user.getUserNum());
        }
        
        if (user.getUserId() != null) {
            session.setAttribute("userId", user.getUserId());
            log.debug("✅ userId 세션 저장: {}", user.getUserId());
        }
        
        if (user.getUsername() != null) {
            session.setAttribute("username", user.getUsername());
            log.debug("✅ username 세션 저장: {}", user.getUsername());
        }
        
        // 팀원 버전 호환용 userInfo 세션
        session.setAttribute("userInfo", user);
        log.debug("✅ userInfo 세션 저장 완료");
        
        return user;
    }
    
    public void logout(HttpSession session) {
        session.invalidate();
    }
    
    // === 중복 체크 ===
    
    public boolean isUserIdAvailable(String userId) {
        return userDAO.checkDuplicateUserId(userId) == 0;
    }
    
    public boolean isEmailAvailable(String email) {
        return userDAO.checkDuplicateEmail(email) == 0;
    }
    
    public boolean isPhoneAvailable(String phone) {
        return userDAO.checkDuplicatePhone(phone) == 0;
    }
    
    // === 사용자 정보 관리 ===
    
    public UserVO getUserWithActivePass(Long userNum) {
        return userDAO.getUserWithActivePass(userNum);
    }
    
    public boolean updateUser(UserVO user) {
        return userDAO.updateUser(user) > 0;
    }
    
    public boolean updatePassword(Long userNum, String currentPassword, String newPassword) throws Exception {
        UserVO user = userDAO.selectByUserNum(userNum);
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new Exception("현재 비밀번호가 올바르지 않습니다.");
        }
        
        String encryptedNewPassword = passwordEncoder.encode(newPassword);
        return userDAO.updatePassword(userNum, encryptedNewPassword) > 0;
    }
    
    public boolean deactivateUser(Long userNum) {
        return userDAO.deactivateUser(userNum) > 0;
    }
    
    // === 아이디/패스워드 찾기 ===
    
    public String findUserId(String email, String phone) {
        return userDAO.findUserIdByEmailAndPhone(email, phone);
    }
    
    public boolean resetPassword(String userId, String email, String newPassword) throws Exception {
        Long userNum = userDAO.findUserNumForPasswordReset(userId, email);
        if (userNum == null) {
            throw new Exception("사용자 정보가 일치하지 않습니다.");
        }
        
        String encryptedPassword = passwordEncoder.encode(newPassword);
        return userDAO.updatePassword(userNum, encryptedPassword) > 0;
    }
    
    // === 실제 CoolSMS 발송 구현 ===
    
    /**
     * 실제 CoolSMS를 통한 SMS 인증 코드 발송
     */
    public boolean sendSMSAuthCode(String phone) {
        try {
            // 6자리 인증번호 생성
            String authCode = String.format("%06d", new Random().nextInt(1000000));
            
            // CoolSMS 메시지 생성
            Message message = new Message();
            message.setFrom(fromNumber); // properties에서 주입받은 발신번호
            message.setTo(phone);
            message.setText("[헬킹] 인증번호는 [" + authCode + "]입니다. 3분 내에 입력해주세요.");
            
            log.info("SMS 발송 시도 - From: {}, To: {}, Code: {}", fromNumber, phone, authCode);
            
            // 메시지 발송
            SingleMessageSentResponse response = messageService.sendOne(new SingleMessageSendingRequest(message));
            
            log.info("CoolSMS 응답 - StatusCode: {}, StatusMessage: {}", 
                     response.getStatusCode(), response.getStatusMessage());
            
            if ("2000".equals(response.getStatusCode())) {
                // 발송 성공 시 인증번호 저장 (3분 유효)
                smsAuthCodes.put(phone, authCode);
                smsAuthTimes.put(phone, System.currentTimeMillis());
                
                log.info("✅ SMS 인증 코드 발송 완료: {}", phone);
                return true;
            } else {
                log.error("❌ SMS 발송 실패: {} - {}", response.getStatusCode(), response.getStatusMessage());
                return false;
            }
            
        } catch (Exception e) {
            log.error("❌ SMS 발송 중 예외 발생: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * SMS 인증번호 확인
     */
    public boolean verifySMSAuthCode(String phone, String code) {
        try {
            String storedCode = smsAuthCodes.get(phone);
            Long sentTime = smsAuthTimes.get(phone);
            
            if (storedCode == null || sentTime == null) {
                log.warn("SMS 인증: 저장된 코드가 없음 - {}", phone);
                return false;
            }
            
            // 3분 유효시간 체크 (180000ms = 3분)
            if (System.currentTimeMillis() - sentTime > 180000) {
                smsAuthCodes.remove(phone);
                smsAuthTimes.remove(phone);
                log.warn("SMS 인증: 시간 만료 - {}", phone);
                return false;
            }
            
            if (storedCode.equals(code)) {
                // 인증 성공 시 데이터 제거
                smsAuthCodes.remove(phone);
                smsAuthTimes.remove(phone);
                log.info("✅ SMS 인증 성공: {}", phone);
                return true;
            } else {
                log.warn("SMS 인증: 코드 불일치 - {} (입력: {}, 저장: {})", phone, code, storedCode);
                return false;
            }
            
        } catch (Exception e) {
            log.error("SMS 인증 확인 중 예외: {}", e.getMessage(), e);
            return false;
        }
    }
    
    // === 팀원 버전 호환 메서드들 ===
    
    /**
     * 팀원 버전 호환: 사용자 ID로 조회
     * @deprecated selectByUserId 사용 권장
     */
    public UserVO getUserById(String user_id) throws Exception {
        return userDAO.getUserById(user_id);
    }
    
    /**
     * 팀원 버전 호환: 방문 시간 업데이트
     */
    public void updateVisteDate(String user_id) throws Exception {
        userDAO.updateVistDate(user_id);
    }
    
    /**
     * 팀원 버전 호환: 계정 상태 변경
     */
    public void deleteYN(UserVO vo) throws Exception {
        userDAO.deleteYN(vo);
    }
    
    // === 이메일 인증 (기존 유지) ===
    
    @Autowired
    private JavaMailSender mailSender;
    
    // 이메일 인증 코드 임시 저장소 (실제로는 Redis 권장)
    private Map<String, String> emailAuthCodes = new HashMap<>();
    private Map<String, Long> emailAuthTimes = new HashMap<>();
    
    public boolean sendEmailAuthCode(String email) {
        try {
            // 6자리 인증번호 생성
            String authCode = String.format("%06d", new Random().nextInt(1000000));
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            
            helper.setFrom("ceskalee@gmail.com");
            helper.setTo(email);
            helper.setSubject("[헬킹] 이메일 인증번호");
            helper.setText("회원가입 인증번호는 <b>" + authCode + "</b> 입니다.<br>3분 내에 입력해주세요.", true);
            
            mailSender.send(message);
            
            // 인증번호 저장 (3분 유효)
            emailAuthCodes.put(email, authCode);
            emailAuthTimes.put(email, System.currentTimeMillis());
            
            log.info("이메일 인증 코드 발송 완료: {}", email);
            return true;
            
        } catch (Exception e) {
            log.error("이메일 발송 실패: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean verifyEmailAuthCode(String email, String code) {
        String storedCode = emailAuthCodes.get(email);
        Long sentTime = emailAuthTimes.get(email);
        
        if (storedCode == null || sentTime == null) {
            return false;
        }
        
        // 3분 유효시간 체크
        if (System.currentTimeMillis() - sentTime > 180000) {
            emailAuthCodes.remove(email);
            emailAuthTimes.remove(email);
            return false;
        }
        
        if (storedCode.equals(code)) {
            emailAuthCodes.remove(email);
            emailAuthTimes.remove(email);
            return true;
        }
        
        return false;
    }
}