package org.example.surveyplanet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SurveyPlanetApplication {

    public static void main(String[] args) {
        SpringApplication.run(SurveyPlanetApplication.class, args);
    }

}
