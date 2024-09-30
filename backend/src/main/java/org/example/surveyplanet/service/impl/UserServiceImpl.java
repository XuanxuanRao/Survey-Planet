package org.example.surveyplanet.service.impl;

import jakarta.annotation.Resource;
import org.example.surveyplanet.common.exception.DuplicateUserInformationException;
import org.example.surveyplanet.common.exception.PasswordErrorException;
import org.example.surveyplanet.common.exception.UserNotFoundException;
import org.example.surveyplanet.common.exception.VerificationCodeErrorException;
import org.example.surveyplanet.mapper.UserMapper;
import org.example.surveyplanet.pojo.dto.UserLoginDTO;
import org.example.surveyplanet.pojo.dto.UserRegisterDTO;
import org.example.surveyplanet.pojo.dto.UserResetDTO;
import org.example.surveyplanet.pojo.entity.User;
import org.example.surveyplanet.service.UserService;
import org.example.surveyplanet.service.VerificationCodeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private VerificationCodeService verificationCodeService;

    @Override
    public User getById(Integer uid) {
        return userMapper.getById(uid);
    }

    @Override
    public User getByUsername(String username) {
        return userMapper.getByUsername(username);
    }

    @Override
    public User login(UserLoginDTO userLoginDTO) {
        String username = userLoginDTO.getUsername();
        String email = userLoginDTO.getEmail();
        String password = userLoginDTO.getPassword();

        if (!StringUtils.hasLength(username) && !StringUtils.hasLength(email)) {
            throw new IllegalArgumentException("USERNAME_AND_EMAIL_BOTH_NULL");
        }
        if (StringUtils.hasLength(username) && StringUtils.hasLength(email)) {
            throw new IllegalArgumentException("USERNAME_AND_EMAIL_BOTH_EXIST");
        }
        User user = StringUtils.hasLength(username) ? userMapper.getByUsername(username) : userMapper.getByEmail(email);
        // 如果用户不存在
        if (user == null) {
            throw new UserNotFoundException("USERNAME_NOT_FOUND");
        }
        // 如果密码不正确
        if (!password.equals(user.getPassword())) {
            throw new PasswordErrorException("PASSWORD_ERROR");
        }

        return user;
    }

    @Override
    public User register(UserRegisterDTO userRegisterDTO) {
        if (userMapper.getByUsername(userRegisterDTO.getUsername()) != null) {
            throw new DuplicateUserInformationException("USERNAME_EXIST");
        }
        if (userMapper.getByEmail(userRegisterDTO.getEmail()) != null) {
            throw new DuplicateUserInformationException("EMAIL_EXIST");
        }
        if (!verificationCodeService.query(userRegisterDTO.getEmail(), userRegisterDTO.getCode())) {
            throw new VerificationCodeErrorException("VERIFICATION_CODE_ERROR");
        }

        User user = User.builder()
                .username(userRegisterDTO.getUsername())
                .email(userRegisterDTO.getEmail())
                .password(userRegisterDTO.getPassword())
                .createTime(LocalDateTime.now())        // 设置账号创建时间为当前时间
                .build();
        userMapper.insert(user);
        verificationCodeService.delete(userRegisterDTO.getEmail());
        return user;
    }

    @Override
    public User reset(UserResetDTO userResetDTO) {
        User user = userMapper.getByEmail(userResetDTO.getEmail());
        if (user == null) {
            throw new UserNotFoundException("EMAIL_NOT_FOUND");
        }
        if (!verificationCodeService.query(userResetDTO.getEmail(), userResetDTO.getCode())) {
            throw new VerificationCodeErrorException("VERIFICATION_CODE_ERROR");
        }

        user.setPassword(userResetDTO.getPassword());
        verificationCodeService.delete(userResetDTO.getEmail());
        userMapper.update(user);
        return user;
    }
}
