package org.example.surveyplanet.service.impl;

import jakarta.annotation.Resource;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.example.surveyplanet.pojo.dto.EmailSendCodeDTO;
import org.example.surveyplanet.pojo.entity.VerificationCode;
import org.example.surveyplanet.service.EmailService;
import org.example.surveyplanet.service.VerificationCodeService;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Resource
    private JavaMailSender mailSender;

    @Resource
    private VerificationCodeService verificationCodeService;

    private static final String REGISTER = "reg";
    private static final String FORGET = "reset";

    public void sendVerificationCode(EmailSendCodeDTO emailSendCodeDTO) {
        String subject = "Verification code";
        // 生成6位数字随机验证码
        String code = String.valueOf(new Random().nextInt(899999) + 100000);

        verificationCodeService.insert(new VerificationCode(emailSendCodeDTO.getEmail(), code));

        String htmlContent;
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("email_register.html");
            if (inputStream == null) {
                log.error("Email template not found");
                throw new RuntimeException("Email template not found");
            }

            // 使用 BufferedReader 和 InputStreamReader 将 InputStream 转换为字符串
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                htmlContent = reader.lines().collect(Collectors.joining("\n")).replace("{{verification_code}}", code);
            } catch (Exception e) {
                throw new RuntimeException("Error reading file", e);
            }
        }  catch (Exception e) {
            throw new RuntimeException("Error reading file", e);
        }

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(emailSendCodeDTO.getEmail());
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true表示发送HTML邮件
            mailSender.send(message);
            log.info("send email to {} successfully", emailSendCodeDTO.getEmail());
        } catch (MailAuthenticationException e) {
            // 处理认证异常
            log.error("Email authentication failed: {}", e.getMessage());
            throw new RuntimeException("Authentication failed while sending email");
        } catch (MailSendException e) {
            // 处理邮件发送异常，通常与网络或SMTP服务器相关
            log.error("Failed to send email to {}. Error: {}", emailSendCodeDTO.getEmail(), e.getMessage());
            throw new RuntimeException("Error occurred while sending email");
        } catch (Exception e) {
            // 捕获所有其他异常
            log.error("Unexpected error while sending email to {}: {}", emailSendCodeDTO.getEmail(), e.getMessage());
            throw new RuntimeException("Unexpected error occurred while sending email");
        }
    }
}
