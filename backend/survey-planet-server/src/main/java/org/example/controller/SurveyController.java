package org.example.controller;

import jakarta.annotation.Resource;
import org.example.Result.Result;
import org.example.dto.survey.CreateSurveyDTO;
import org.example.entity.response.Response;
import org.example.entity.Survey;
import org.example.entity.question.Question;
import org.example.enumeration.SurveyState;
import org.example.exception.IllegalRequestException;
import org.example.exception.SurveyNotFoundException;
import org.example.service.QuestionService;
import org.example.service.ResponseService;
import org.example.service.SurveyService;
import org.example.vo.question.CreatedQuestionVO;
import org.example.vo.survey.CreatedSurveyVO;
import org.example.context.BaseContext;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api")
public class SurveyController {

    @Resource
    private SurveyService surveyService;
    @Resource
    private QuestionService questionService;

    @Resource
    private ResponseService responseService;

    @GetMapping("/survey/list")
    public Result<List<CreatedSurveyVO>> getSurveys(
            @RequestParam String type,  // 查找创建的问卷或是填写过的问卷
            @RequestParam(defaultValue = "create_time") String sort)
    {
        if (!"created".equals(type) && !"participated".equals(type)) {
            throw new IllegalRequestException("", "Invalid type " + type);
        }
        if (!"create_time".equals(sort) && !"update_time".equals(sort)) {
            throw new IllegalRequestException("", "Invalid sort " + sort);
        }

        return Result.success(
            surveyService.getSurveys(BaseContext.getCurrentId(), "created".equals(type), sort).stream().map(s -> {
                CreatedSurveyVO createdSurveyVO = CreatedSurveyVO.builder()
                        .questions(transfer(questionService.getBySid(s.getSid())))
                        .type(s.getType().getValue())
                        .state(s.getState().getValue())
                        .build();
                BeanUtils.copyProperties(s, createdSurveyVO);
                return createdSurveyVO;
            })
            .collect(Collectors.toList())
        );
    }

    @GetMapping("/survey/{sid}")
    public Result<CreatedSurveyVO> getSurvey(@PathVariable Long sid) {
        // 获取问卷
        Survey survey = surveyService.getSurvey(sid);
        if (survey == null || !Objects.equals(survey.getUid(), BaseContext.getCurrentId())) {
            throw new SurveyNotFoundException("Survey not found");
        }

        // 获取问卷的问题
        List<Question> questions = questionService.getBySid(sid);

        CreatedSurveyVO createdSurveyVO = CreatedSurveyVO.builder()
                        .questions(transfer(questions))
                        .type(survey.getType().getValue())
                        .state(survey.getState().getValue())
                        .build();
        BeanUtils.copyProperties(survey, createdSurveyVO);

        return Result.success(createdSurveyVO);
    }

    @PostMapping("/survey/add")
    public Result<Long> createSurvey(@RequestBody CreateSurveyDTO createdSurveyDTO) {
        Long sid = surveyService.addSurvey(createdSurveyDTO).getSid();
        questionService.addQuestions(createdSurveyDTO.getQuestions(), sid);

        return Result.success(sid);
    }

    @PutMapping("/survey/{sid}")
    public Result<Void> modifySurvey(@PathVariable Long sid, @RequestBody CreateSurveyDTO createdSurveyDTO) {
        surveyService.updateSurvey(sid, createdSurveyDTO);

        return Result.success();
    }

    @PostMapping("/survey/{sid}/share")
    public Result<String> shareSurvey(@PathVariable Long sid) {
        return Result.success(surveyService.shareSurvey(sid));
    }

    @PutMapping("survey/{sid}/close")
    public Result<Void> closeSurvey(@PathVariable Long sid) {
        surveyService.closeSurvey(sid);
        return Result.success();
    }

    @DeleteMapping("/survey/{sid}")
    public Result<Void> deleteSurvey(@PathVariable Long sid) {
        surveyService.modifyState(sid, SurveyState.DELETE);
        return Result.success();
    }

    @GetMapping("/survey/{sid}/response/detail")
    public Result<List<Response>> getResponses(@PathVariable Long sid) {
        return Result.success(responseService.getResponseBySid(sid));
    }


    private List<CreatedQuestionVO> transfer(List<Question> questions) {
        return questions.stream().map(q -> {
            CreatedQuestionVO questionVO = new CreatedQuestionVO();
            questionVO.setType(q.getType().getValue());
            BeanUtils.copyProperties(q, questionVO);
            return questionVO;
        }).collect(Collectors.toList());
    }

}
