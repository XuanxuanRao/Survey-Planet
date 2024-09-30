package org.example.surveyplanet.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationCode {
    private static final Long validityPeriod = 3 * 60L;
    private String email;
    private String code;
    private LocalDateTime expireTime;

    public VerificationCode(String email, String code) {
        this.email = email;
        this.code = code;
        this.expireTime = LocalDateTime.now().plusSeconds(validityPeriod);
    }
}
