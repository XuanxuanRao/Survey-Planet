package org.example.surveyplanet.pojo.vo;

import lombok.*;
import org.example.surveyplanet.pojo.entity.question.Question;
import org.example.surveyplanet.pojo.entity.survey.Survey;

import java.util.List;


@Builder
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatedSurveyVO extends Survey {
    private String type;
    private List<Question> questions;
}
