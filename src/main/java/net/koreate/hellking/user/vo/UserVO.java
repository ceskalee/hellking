package net.koreate.hellking.user.vo;

import lombok.Data;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 통합 사용자 VO 클래스
 * - 팀원 버전과 호환성 유지
 * - 당신 버전의 구조적 개선 사항 포함
 */
@Data
public class UserVO {
    
    // === 기본 사용자 정보 ===
    private Long userNum;        // user_num (Primary Key)
    private String userId;       // user_id (로그인용 아이디)
    private String username;     // username (실명)
    private String email;        // email (분리된 이메일 필드)
    private String phone;        // phone (전화번호)
    private String password;     // password (BCrypt 암호화)
    private String profileImage; // profile_image (프로필 이미지)
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthDate;      // birth_date (생년월일)
    
    private String gender;       // gender (성별: M/F)
    private Date joinDate;       // join_date (가입일)
    private String status;       // status (계정 상태: ACTIVE/INACTIVE)
    
    // === 팀원 버전 호환성을 위한 메서드 ===
    
    /**
     * 팀원 버전 호환: user_id getter
     * @deprecated getUserId() 사용 권장
     */
    public String getUser_id() {
        return this.userId;
    }
    
    /**
     * 팀원 버전 호환: user_id setter  
     * @deprecated setUserId() 사용 권장
     */
    public void setUser_id(String userId) {
        this.userId = userId;
    }
    
    /**
     * 팀원 버전 호환: user_num getter
     * @deprecated getUserNum() 사용 권장
     */
    public Long getUser_num() {
        return this.userNum;
    }
    
    /**
     * 팀원 버전 호환: user_num setter
     * @deprecated setUserNum() 사용 권장
     */
    public void setUser_num(Long userNum) {
        this.userNum = userNum;
    }
    
    /**
     * 팀원 버전 호환: profile_image getter
     * @deprecated getProfileImage() 사용 권장
     */
    public String getProfile_image() {
        return this.profileImage;
    }
    
    /**
     * 팀원 버전 호환: profile_image setter
     * @deprecated setProfileImage() 사용 권장  
     */
    public void setProfile_image(String profileImage) {
        this.profileImage = profileImage;
    }
    
    /**
     * 팀원 버전 호환: birth_date getter
     * @deprecated getBirthDate() 사용 권장
     */
    public Date getBirth_date() {
        return this.birthDate;
    }
    
    /**
     * 팀원 버전 호환: birth_date setter
     * @deprecated setBirthDate() 사용 권장
     */
    public void setBirth_date(Date birthDate) {
        this.birthDate = birthDate;
    }
    
    /**
     * 팀원 버전 호환: join_date getter
     * @deprecated getJoinDate() 사용 권장
     */
    public Date getJoin_date() {
        return this.joinDate;
    }
    
    /**
     * 팀원 버전 호환: join_date setter
     * @deprecated setJoinDate() 사용 권장
     */
    public void setJoin_date(Date joinDate) {
        this.joinDate = joinDate;
    }
    
    // === 조인용 추가 필드 (패스권 정보 등) ===
    private String passName;     // 활성 패스권 이름
    private Date passEndDate;    // 패스권 만료일
    private String passStatus;   // 패스권 상태
    
    // === 유틸리티 메서드 ===
    
    /**
     * 관리자 권한 체크
     */
    public boolean isAdmin() {
        return "admin".equals(this.userId) || "aqunasl22".equals(this.userId);
    }
    
    /**
     * 활성 계정 체크
     */
    public boolean isActive() {
        return "ACTIVE".equals(this.status);
    }
    
    /**
     * 프로필 이미지 경로 반환 (기본 이미지 처리)
     */
    public String getProfileImagePath() {
        if (profileImage == null || profileImage.trim().isEmpty()) {
            return "avatar1.png";
        }
        return profileImage;
    }
}