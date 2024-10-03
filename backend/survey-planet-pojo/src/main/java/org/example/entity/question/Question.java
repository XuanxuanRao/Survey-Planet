package org.example.entity.question;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enumeration.QuestionType;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    private Long qid;
    private Long sid;
    private String title;
    private String description;
    private QuestionType type;
    private Boolean required;
    private Integer score;
}
