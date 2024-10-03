package org.example.service;


import org.example.dto.CreateSurveyDTO;
import org.example.entity.Survey;

import java.util.List;

public interface SurveyService {

    Survey getSurvey(Long sid);

    List<Survey> getSurveys(Long uid, boolean isCreated, String sortBy);

    Survey addSurvey(CreateSurveyDTO surveyDTO);

    void updateSurvey(Long sid, CreateSurveyDTO createdSurveyDTO);
}
