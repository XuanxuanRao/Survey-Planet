package org.example.surveyplanet.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.surveyplanet.mapper.SurveyMapper;
import org.example.surveyplanet.pojo.dto.SurveyDTO;
import org.example.surveyplanet.pojo.entity.survey.Survey;
import org.example.surveyplanet.service.SurveyService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SurveyServiceImpl implements SurveyService {
    @Resource
    SurveyMapper surveyMapper;

    @Override
    public List<Survey> getSurveys(Integer uid, boolean isCreated, String sortBy) {
        if (isCreated) {
            return surveyMapper.getCreatedList(uid, sortBy);
        }
        // todo: get answered list
        return new ArrayList<>();
    }

    @Override
    public void createSurvey(SurveyDTO surveyDTO) {
        surveyDTO.setCreateTime(LocalDateTime.now());
        surveyDTO.setUpdateTime(LocalDateTime.now());
        surveyDTO.setStatus(Survey.Status.CLOSE);
        surveyDTO.setFillNum(0);

        surveyMapper.insert(surveyDTO);
    }

    public Survey getById(Integer id) {
        return surveyMapper.getById(id);
    }
}

