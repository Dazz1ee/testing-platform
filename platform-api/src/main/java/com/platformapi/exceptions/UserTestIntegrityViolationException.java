package com.platformapi.exceptions;

import org.springframework.http.HttpStatus;

public class UserTestIntegrityViolationException extends CustomException {
    public UserTestIntegrityViolationException(Throwable ex) {
        super(HttpStatus.BAD_REQUEST, ex);
    }
}
