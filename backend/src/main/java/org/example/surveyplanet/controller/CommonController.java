package org.example.surveyplanet.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.surveyplanet.common.Result.Result;
import org.example.surveyplanet.common.utils.AliOSSUtil;
import org.example.surveyplanet.pojo.dto.EmailSendCodeDTO;
import org.example.surveyplanet.service.impl.EmailServiceImpl;
import org.springframework.web.bind.annotation.*;
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
    EmailServiceImpl emailService;

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
