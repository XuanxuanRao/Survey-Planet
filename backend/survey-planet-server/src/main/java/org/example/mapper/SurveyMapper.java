package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.annotation.AutoFill;
import org.example.dto.CreateSurveyDTO;
import org.example.entity.Survey;
import org.example.enumeration.OperationType;

import java.util.List;


@Mapper
public interface SurveyMapper {

    Survey getBySid(Long sid);

    /**
     * Get the list of surveys created by the user whose id is {@code userId},
     * and sort the list by keyword {@code sortBy}.
     * @param userId the id of the user
     * @param orderBy the keyword to sort the list
     * @return the list of sorted surveys
     */
    List<Survey> getCreatedList(Long userId, String orderBy);

    @AutoFill(value = OperationType.INSERT)
    void insert(Survey survey);

    @AutoFill(value = OperationType.UPDATE)
    void update(Survey survey);

}
