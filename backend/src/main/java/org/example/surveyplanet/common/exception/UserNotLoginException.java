package org.example.surveyplanet.common.exception;

public class UserNotLoginException extends BusinessException {
    public UserNotLoginException() {
    }

    public UserNotLoginException(String msg) {
        super(msg);
    }
}
