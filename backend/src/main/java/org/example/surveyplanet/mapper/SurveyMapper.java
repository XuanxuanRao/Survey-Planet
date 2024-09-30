package org.example.surveyplanet.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.surveyplanet.pojo.entity.survey.Survey;

import java.util.List;

@Mapper
public interface SurveyMapper {


    /**
     * Get the list of surveys created by the user whose id is {@code userId},
     * and sort the list by keyword {@code sortBy}.
     * @param userId the id of the user
     * @param sortBy the keyword to sort the list
     * @return the list of sorted surveys
     */
    List<Survey> getCreatedList(Integer uid, String orderBy);

    void insert(Survey survey);

    Survey getById(Integer sid);

}
