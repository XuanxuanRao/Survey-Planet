package org.example.service;


import org.example.dto.UserLoginDTO;
import org.example.dto.UserRegisterDTO;
import org.example.dto.UserResetDTO;
import org.example.entity.User;

public interface UserService {
    User getById(Long uid);

    User getByUsername(String username);

    User login(UserLoginDTO userLoginDTO);

    User register(UserRegisterDTO userRegisterDTO);

    User resetPassword(UserResetDTO userResetDTO);

    User getByEmail(String email);
}
