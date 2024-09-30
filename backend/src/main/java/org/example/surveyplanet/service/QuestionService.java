package org.example.surveyplanet.service;

import org.example.surveyplanet.pojo.dto.QuestionDTO;
import org.example.surveyplanet.pojo.entity.question.Question;

import java.util.List;

public interface QuestionService {
    void addQuestions(List<QuestionDTO> questions);

    void deleteQuestions(List<Question> questions);

    /**
     * 获取id为sid的问卷的问题列表
     * @param sid 要查询问卷的id
     * @return 问题列表
     */
    List<Question> getQuestions(Integer sid);

    void updateQuestions(List<QuestionDTO> questions);
}
