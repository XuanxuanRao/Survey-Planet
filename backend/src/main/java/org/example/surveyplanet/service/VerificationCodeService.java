package org.example.surveyplanet.service;

import org.example.surveyplanet.pojo.dto.UserRegisterDTO;
import org.example.surveyplanet.pojo.entity.VerificationCode;

public interface VerificationCodeService {
    void insert(VerificationCode verificationCode);

    boolean query(String email, String code);

    Integer clear();

    Integer delete(String email);
}
