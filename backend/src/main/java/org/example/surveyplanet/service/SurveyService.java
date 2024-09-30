package org.example.surveyplanet.service;

import org.example.surveyplanet.pojo.dto.SurveyDTO;
import org.example.surveyplanet.pojo.entity.survey.Survey;

import java.util.List;

public interface SurveyService {
    List<Survey> getSurveys(Integer uid, boolean isCreated, String sortBy);

    void createSurvey(SurveyDTO surveyQueryDTO);

    Survey getById(Integer id);
}
