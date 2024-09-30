package org.example.surveyplanet.common.exception;


public class BusinessException extends RuntimeException {
    public BusinessException() {
    }

    public BusinessException(String msg) {
        super(msg);
    }
}
