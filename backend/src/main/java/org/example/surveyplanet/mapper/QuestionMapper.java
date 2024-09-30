package org.example.surveyplanet.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.surveyplanet.pojo.dto.QuestionDTO;
import org.example.surveyplanet.pojo.entity.question.Question;

import java.util.List;

@Mapper
public interface QuestionMapper {

    void insert(List<QuestionDTO> questions);

    List<Question> getBySid(Integer sid);

    void delete(List<Question> questions);

    void update(List<QuestionDTO> questions);
}
