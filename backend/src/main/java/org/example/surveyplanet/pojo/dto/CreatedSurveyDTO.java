package org.example.surveyplanet.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatedSurveyDTO {
    private String title;
    private String description;
    private String type;
    private List<QuestionDTO> questions;
}
