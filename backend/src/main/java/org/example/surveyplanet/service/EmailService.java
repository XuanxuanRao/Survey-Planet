package org.example.surveyplanet.service;

import org.example.surveyplanet.pojo.dto.EmailSendCodeDTO;

public interface EmailService {
    void sendVerificationCode(EmailSendCodeDTO emailSendCodeDTO);
}
