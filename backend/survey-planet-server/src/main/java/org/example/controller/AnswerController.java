package org.example.controller;

import jakarta.annotation.Resource;
import org.example.Result.Result;
import org.example.dto.ResponseDTO;
import org.example.entity.Survey;
import org.example.entity.question.Question;
import org.example.enumeration.SurveyState;
import org.example.exception.IllegalRequestException;
import org.example.exception.SurveyNotFoundException;
import org.example.service.QuestionService;
import org.example.service.ResponseService;
import org.example.service.SurveyService;
import org.example.utils.SharingCodeUtil;
import org.example.vo.survey.FilledSurveyVO;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@RestController
@RequestMapping("/api")
public class AnswerController {

    @Resource
    private ResponseService responseService;

    @Resource
    private SurveyService surveyService;

    @Resource
    private QuestionService questionService;

    @PostMapping("/submit")
    public Result<String> submit(@RequestBody ResponseDTO responseDTO) {
        responseService.submit(responseDTO);

        // assure the response is successful
        return Result.success("submit success");
    }

    @GetMapping("/fill/{code}")
    public Result<FilledSurveyVO> show(@PathVariable String code) {
        Long sid;
        try {
            sid = SharingCodeUtil.decrypt(code);
        } catch (Exception e) {
            throw new IllegalRequestException(this.getClass().getName(), "Bad URL");
        }

        Survey survey = surveyService.getSurvey(sid);
        if (survey == null || survey.getState() != SurveyState.OPEN) {
            throw new SurveyNotFoundException("Survey not found or not open");
        }

        FilledSurveyVO surveyVO = FilledSurveyVO.builder()
                .questions(questionService.getBySid(sid).stream().map(Question::toFilledQuestionVO).toList())
                .type(survey.getType().getValue())
                .state(survey.getState().getValue())
                .build();
        BeanUtils.copyProperties(survey, surveyVO);

        return Result.success(surveyVO);
    }

}
