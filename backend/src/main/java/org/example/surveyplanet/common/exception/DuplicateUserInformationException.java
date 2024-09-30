package org.example.surveyplanet.common.exception;

public class DuplicateUserInformationException extends BusinessException {
    public DuplicateUserInformationException() {
    }

    public DuplicateUserInformationException(String emailExist) {
        super(emailExist);
    }
}
