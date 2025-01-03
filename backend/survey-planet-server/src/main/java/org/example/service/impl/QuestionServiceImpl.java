package org.example.service.impl;

import jakarta.annotation.Resource;
import org.example.dto.QuestionDTO;
import org.example.entity.question.*;
import org.example.mapper.QuestionMapper;
import org.example.service.QuestionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class QuestionServiceImpl implements QuestionService {

    @Resource
    private QuestionMapper questionMapper;

    @Override
    public List<Question> getBySid(Long sid) {
        return questionMapper.getQuestionsBySid(sid);
    }

    @Override
    public Question getByQid(Long qid) {
        return questionMapper.getQuestionByQid(qid);
    }

    @Override
    @Transactional
    public void addQuestions(List<QuestionDTO> questions, Long sid) {
        if (questions == null || questions.isEmpty()) {
            return;
        }

        // 将 DTO 转为为 Question 实体
        List<Question> questionList = questions.stream()
                .map(questionDTO -> questionDTO.toQuestionEntity(questionDTO, sid))
                .toList();

        questionMapper.insertBaseQuestions(questionList);

        questionList.forEach(question -> {
            switch (question.getType()) {
                case SINGLE_CHOICE -> questionMapper.insertSingleChoiceQuestion((SingleChoiceQuestion) question);
                case MULTIPLE_CHOICE -> questionMapper.insertMultipleChoiceQuestion((MultipleChoiceQuestion) question);
                case FILL_BLANK -> questionMapper.insertFillBlankQuestion((FillBlankQuestion) question);
                case FILE -> questionMapper.insertFileQuestion((FileQuestion) question);
                case CODE -> questionMapper.insertCodeQuestion((CodeQuestion) question);
            }
        });
    }


    @Override
    public void deleteQuestions(List<Question> questions) {
        if (questions == null || questions.isEmpty()) {
            return;
        }

        questions.forEach(question -> {
            switch (question.getType()) {
                case SINGLE_CHOICE -> questionMapper.deleteSingleChoiceQuestion(question.getQid());
                case MULTIPLE_CHOICE -> questionMapper.deleteMultipleChoiceQuestion(question.getQid());
                case FILL_BLANK -> questionMapper.deleteFillBlankQuestion(question.getQid());
                case FILE -> questionMapper.deleteFileQuestion(question.getQid());
                case CODE -> questionMapper.deleteCodeQuestion(question.getQid());
            }
        });
        questionMapper.delete(questions.stream().map(Question::getQid).toList());
    }
}
