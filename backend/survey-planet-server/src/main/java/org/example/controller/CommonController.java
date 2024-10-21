package org.example.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.example.Result.Result;
import org.example.dto.EmailSendCodeDTO;
import org.example.service.EmailService;
import org.example.utils.AliOSSUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;


@Slf4j
@RestController
@RequestMapping("/api/common")
public class CommonController {
    @Resource
    AliOSSUtil aliOssUtil;
    @Resource
    EmailService emailService;

    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            assert originalFilename != null;
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            // 构造新文件名称
            String objectName = UUID.randomUUID() + extension;
            // 文件的请求路径
            String path = aliOssUtil.upload(file.getBytes(), objectName);
            // 返回文件路径
            return Result.success(path);
        } catch (IOException e) {
            log.error("File upload failed", e);
        }
        return Result.error("file upload failed");
    }

    @PostMapping("/email/code")
    public Result<String> sendCode2Email(@RequestBody EmailSendCodeDTO emailSendCodeDTO) {
        emailService.sendVerificationCode(emailSendCodeDTO);
        return Result.success("Email sent successfully");
    }
}