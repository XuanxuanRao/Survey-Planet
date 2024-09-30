package org.example.surveyplanet;

import org.example.surveyplanet.pojo.entity.question.Options;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SurveyPlanetApplicationTests {

    @Test
    void contextLoads() {
        new Options("[\"abc\", \"def\"]").getOptions().forEach(
            option -> System.out.println(option.getId() + ": " + option.getContent())
        );
    }

}
