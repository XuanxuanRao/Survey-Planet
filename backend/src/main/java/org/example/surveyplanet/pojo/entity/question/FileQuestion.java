package org.example.surveyplanet.pojo.entity.question;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class FileQuestion extends Question {
    /**
     * Maximum file size allow to upload(MB)
     */
    private Integer maxFileSize;

    public FileQuestion(Integer qid, Integer sid, String title, String description, Integer maxFileSize, Boolean required) {
        super(qid, sid, title, description, required);
        this.maxFileSize = maxFileSize;
    }

    public FileQuestion() {
        super();
        this.maxFileSize = 5;
    }
}
