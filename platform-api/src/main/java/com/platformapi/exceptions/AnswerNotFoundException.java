package com.platformapi.exceptions;

import org.springframework.http.HttpStatus;

public class AnswerNotFoundException extends CustomException {
    public AnswerNotFoundException(Throwable exception) {
        super(HttpStatus.BAD_REQUEST, "incorrect data", exception);
    }
}
