package com.platformapi.exceptions;

import org.springframework.http.HttpStatus;

public class UnknownCustomException extends CustomException {
    public UnknownCustomException(Throwable ex) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }
}
