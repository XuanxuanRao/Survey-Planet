package org.example.surveyplanet.controller;


import jakarta.annotation.Resource;
import org.example.surveyplanet.common.Result.Result;
import org.example.surveyplanet.common.context.BaseContext;
import org.example.surveyplanet.pojo.dto.CreatedSurveyDTO;
import org.example.surveyplanet.pojo.dto.QuestionDTO;
import org.example.surveyplanet.pojo.dto.SurveyDTO;
import org.example.surveyplanet.pojo.entity.question.Question;
import org.example.surveyplanet.pojo.entity.survey.ExamSurvey;
import org.example.surveyplanet.pojo.entity.survey.NormalSurvey;
import org.example.surveyplanet.pojo.entity.survey.Survey;
import org.example.surveyplanet.pojo.vo.CreatedSurveyVO;
import org.example.surveyplanet.service.QuestionService;
import org.example.surveyplanet.service.SurveyService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SurveyController {
    @Resource
    SurveyService surveyService;

    @Resource
    QuestionService questionService;

    @GetMapping("/survey/list")
    public Result<List<CreatedSurveyVO>> getSurveys(
            @RequestParam String type,
            @RequestParam(defaultValue = "create_time") String sort
    ) {
        if (!"created".equals(type) && !"participated".equals(type)) {
            throw new IllegalArgumentException("Invalid type: " + type);
        }

        return Result.success(
            surveyService.getSurveys(BaseContext.getCurrentId(), "created".equals(type), sort).stream().map(s -> {
                CreatedSurveyVO createdSurveyVO = CreatedSurveyVO.builder()
                        .questions(questionService.getQuestions(s.getSid()))
                        .type(getSurveyType(s))
                        .build();
                BeanUtils.copyProperties(s, createdSurveyVO);
                return createdSurveyVO;
            })
            .collect(Collectors.toList())
        );
    }

    @PostMapping("/survey/add")
    public Result<Void> createSurvey(@RequestBody CreatedSurveyDTO createdSurveyDTO) {
        SurveyDTO surveyDTO = new SurveyDTO();
        BeanUtils.copyProperties(createdSurveyDTO, surveyDTO);
        surveyDTO.setUid(BaseContext.getCurrentId());
        surveyService.createSurvey(surveyDTO);

        createdSurveyDTO.getQuestions().forEach(q -> {
            // 设置问题的sid，与问卷关联
            q.setSid(surveyDTO.getSid());
            // 将List<String>类型的选项列表转化为json格式字符串
            q.setOptionsJson();
        });
        questionService.addQuestions(createdSurveyDTO.getQuestions());

        return Result.success();
    }

    @GetMapping("/survey/{sid}")
    public Result<CreatedSurveyVO> getSurvey(@PathVariable Integer sid) {
        Survey survey = surveyService.getById(sid);

        CreatedSurveyVO createdSurveyVO = CreatedSurveyVO.builder()
                .questions(questionService.getQuestions(sid))
                .type(getSurveyType(survey))
                .build();
        BeanUtils.copyProperties(survey, createdSurveyVO);
        return Result.success(createdSurveyVO);
    }

    @PutMapping("/survey/{sid}")
    public Result<CreatedSurveyVO> modifySurvey(@PathVariable Integer sid, @RequestBody CreatedSurveyDTO createdSurveyDTO) {
        Survey survey = surveyService.getById(sid);
        if (survey == null) {
            throw new IllegalArgumentException("Survey not found: " + sid);
        } else if (survey.getOpenTime() != null) {
            throw new IllegalArgumentException("Survey has been opened: " + sid);
        }

        createdSurveyDTO.getQuestions().forEach(q -> {
            // 设置问题的sid，与问卷关联
            q.setSid(sid);
            // 将List<String>类型的选项列表转化为json格式字符串
            q.setOptionsJson();
        });
        List<Question> oriQuestions = questionService.getQuestions(sid);

        List<Question> deletedQuestions = oriQuestions.stream()
                .filter(q -> createdSurveyDTO.getQuestions().stream().noneMatch(qd -> q.getQid().equals(qd.getQid())))
                .toList();
        List<QuestionDTO> addedQuestions = createdSurveyDTO.getQuestions().stream()
                .filter(qd -> qd.getQid() == null)
                .toList();
        List<QuestionDTO> modifiedQuestions = createdSurveyDTO.getQuestions().stream()
                .filter(qd -> oriQuestions.stream().anyMatch(q -> q.getQid().equals(qd.getQid())))
                .toList();

        // 修改问题
        questionService.updateQuestions(modifiedQuestions);
        // 添加问题
        questionService.addQuestions(addedQuestions);
        // 删除问题
        questionService.deleteQuestions(deletedQuestions);

        return getSurvey(sid);
    }

    private static String getSurveyType(Survey survey) {
        if (survey instanceof NormalSurvey) {
            return "normal";
        } else if (survey instanceof ExamSurvey) {
            return "exam";
        }
        throw new IllegalArgumentException("Unknown survey type: " + survey.getClass().getName());
    }
}
