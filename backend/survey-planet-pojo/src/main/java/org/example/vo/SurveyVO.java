package org.example.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.example.entity.question.Question;
import org.example.enumeration.SurveyState;
import org.example.enumeration.SurveyType;

import java.util.List;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyVO {
    private Long sid;
    private String title;
    private String description;
    private SurveyState state;
    private SurveyType type;
    private Integer timeLimit;
}
