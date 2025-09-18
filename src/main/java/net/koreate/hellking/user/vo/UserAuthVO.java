package net.koreate.hellking.user.vo;

import lombok.Data;
import java.util.Date;

@Data
public class UserAuthVO {
    private Long authNum;     // auth_num
    private Long userNum;     // user_num  
    private String authType;  // auth_type: 'SMS', 'EMAIL'
    private String authCode;  // auth_code: 인증번호
    private Date expireTime;  // expire_time: 만료시간
    private String isVerified; // is_verified: 'Y'/'N'
    private Date createdAt;   // created_at: 생성시간
}