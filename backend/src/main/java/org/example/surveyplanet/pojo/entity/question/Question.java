package org.example.surveyplanet.pojo.entity.question;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FileQuestion.class, name = "file"),
        @JsonSubTypes.Type(value = MultipleChoiceQuestion.class, name = "single"),
        @JsonSubTypes.Type(value = MultipleSelectQuestion.class, name = "multiple")
})

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Question {
    private Integer qid;
    private Integer sid;
    private String title;
    private String description;
    private Boolean required;
}
