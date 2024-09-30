package org.example.surveyplanet.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResetDTO {
    private String email;
    private String password;
    private String code;
}
