package com.platformapi.exceptions;

import org.springframework.http.HttpStatus;

public class DuplicateAnswerException extends CustomException {
    public DuplicateAnswerException(Throwable ex) {
        super(HttpStatus.BAD_REQUEST, "the answer already exists", ex);
    }
}
