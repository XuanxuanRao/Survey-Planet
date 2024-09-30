package org.example.surveyplanet.pojo.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginDTO implements Serializable {
    private String username;
    private String email;
    private String password;
}
