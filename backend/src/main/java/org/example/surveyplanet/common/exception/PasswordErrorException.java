package org.example.surveyplanet.common.exception;

public class PasswordErrorException extends BusinessException {
    public PasswordErrorException() {
    }

    public PasswordErrorException(String msg) {
        super(msg);
    }
}
