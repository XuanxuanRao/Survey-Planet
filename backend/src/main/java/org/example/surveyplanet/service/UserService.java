package org.example.surveyplanet.service;


import org.example.surveyplanet.pojo.dto.UserLoginDTO;
import org.example.surveyplanet.pojo.dto.UserRegisterDTO;
import org.example.surveyplanet.pojo.dto.UserResetDTO;
import org.example.surveyplanet.pojo.entity.User;

public interface UserService {

    User login(UserLoginDTO userLoginDTO);

    User register(UserRegisterDTO userRegisterDTO);

    User getById(Integer uid);

    User getByUsername(String username);

    User reset(UserResetDTO userResetDTO);
}
