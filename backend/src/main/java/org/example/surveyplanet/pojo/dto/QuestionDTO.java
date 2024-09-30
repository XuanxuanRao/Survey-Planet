package org.example.surveyplanet.pojo.dto;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
    private Integer qid;
    private Integer sid;
    private String title;
    private String description;
    private String type;
    private List<String> options;
    private String optionsJson;
    private Integer maxFileSize;
    private Boolean required;

    public void setOptionsJson() {
        if (this.options == null) {
            this.optionsJson = null;
            return;
        }
        this.optionsJson = new Gson().toJson(this.options);
    }

    public void setOptions() {
        if (this.optionsJson == null) {
            this.options = null;
            return;
        }
        this.options = new Gson().fromJson(this.optionsJson, List.class);
    }
}
