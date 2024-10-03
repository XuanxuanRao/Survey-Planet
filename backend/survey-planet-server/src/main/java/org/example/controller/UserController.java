package org.example.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.example.Result.Result;
import org.example.context.BaseContext;
import org.example.dto.UserLoginDTO;
import org.example.dto.UserRegisterDTO;
import org.example.dto.UserResetDTO;
import org.example.entity.User;
import org.example.service.UserService;
import org.example.utils.JwtUtil;
import org.example.vo.UserLoginVO;
import org.example.vo.UserResetVO;
import org.example.vo.UserRegisterVO;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;


@Slf4j
@RestController
@RequestMapping("/api")
public class UserController {
    @Resource
    private UserService userService;

    @GetMapping("/user")
    public Result<User> get() {
        return Result.success(userService.getById(BaseContext.getCurrentId()));
    }

    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO loginDTO) {
        User result = userService.login(loginDTO);

        // 登录成功，下发JWT
        String JWT = JwtUtil.generateJwt(new HashMap<>() {{
            put("id", result.getUid());
        }});

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .uid(result.getUid())
                .token(JWT)
                .build();
        return Result.success(userLoginVO);
    }

    @PostMapping("/register")
    public Result<UserRegisterVO> register(@RequestBody UserRegisterDTO userDTO) {
        User user = userService.register(userDTO);

        UserRegisterVO userRegisterVO = UserRegisterVO.builder()
                .uid(userService.getByUsername(user.getUsername()).getUid())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
        log.info("{}({}) registered successfully", user.getUsername(), user.getEmail());
        return Result.success(userRegisterVO);
    }


    @PutMapping("/reset")
    public Result<UserResetVO> reset(@RequestBody UserResetDTO userResetDTO) {
        User user = userService.resetPassword(userResetDTO);

        UserResetVO userResetVO = UserResetVO.builder()
                .uid(user.getUid())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
        log.info("{}({}) reset password successfully", user.getUsername(), user.getEmail());
        return Result.success(userResetVO);
    }


}
