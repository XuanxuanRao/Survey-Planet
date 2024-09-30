package org.example.surveyplanet.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.surveyplanet.pojo.entity.survey.Survey;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyDTO extends Survey {
    private String type;
}
