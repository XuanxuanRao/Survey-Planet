package org.example.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateSurveyDTO {
    private Long uid;
    private String type;
    private String title;
    private String description;
    private List<QuestionDTO> questions;
    private Integer timeLimit;
}
