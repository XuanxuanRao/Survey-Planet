package org.example.surveyplanet.service.impl;

import jakarta.annotation.Resource;
import org.example.surveyplanet.mapper.QuestionMapper;
import org.example.surveyplanet.pojo.dto.QuestionDTO;
import org.example.surveyplanet.pojo.entity.question.Question;
import org.example.surveyplanet.service.QuestionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionServiceImpl implements QuestionService {
    @Resource
    private QuestionMapper questionMapper;

    @Override
    public void addQuestions(List<QuestionDTO> questions) {
        if (questions == null || questions.isEmpty()) {
            return;
        }
        questionMapper.insert(questions);
    }

    @Override
    public List<Question> getQuestions(Integer sid) {
        return questionMapper.getBySid(sid);
    }

    @Override
    public void deleteQuestions(List<Question> questions) {
        if (questions == null || questions.isEmpty()) {
            return;
        }
        questionMapper.delete(questions);
    }

    @Override
    public void updateQuestions(List<QuestionDTO> questions) {
        if (questions == null || questions.isEmpty()) {
            return;
        }
        questionMapper.update(questions);
    }
}
