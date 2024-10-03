package org.example.dto;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
    private String title;
    private String description;
    private String type;
    private Boolean required;
    private Integer score;

    private List<String> answer;

    // 单选题和多选题
    /**
     *  接收请求参数时的选项表示
     */
    private List<String> options;

    // 文件上传题
    private Integer maxFileSize;
}
