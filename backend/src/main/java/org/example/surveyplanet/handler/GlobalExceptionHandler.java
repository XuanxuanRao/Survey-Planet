package org.example.surveyplanet.handler;

import lombok.extern.slf4j.Slf4j;
import org.example.surveyplanet.common.Result.Result;
import org.example.surveyplanet.common.exception.BusinessException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public Result<String> exceptionHandler(BusinessException ex) {
        log.error("Business Exception: {}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler
    public Result<String> exceptionHandler(IllegalArgumentException ex) {
        log.error("Argument Exception: {}", ex.getMessage());
        return Result.error(ex.getMessage());
    }
}
