package org.example.surveyplanet.common.exception;

public class VerificationCodeErrorException extends BusinessException {
    public VerificationCodeErrorException() {
    }

    public VerificationCodeErrorException(String msg) {
        super(msg);
    }
}
