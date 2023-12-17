package com.platformapi.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {
    private HttpStatus httpStatus;
    private String errorMessage;

    protected CustomException(HttpStatus httpStatus, String errorMessage) {
        this.errorMessage = errorMessage;
        this.httpStatus = httpStatus;
    }

    protected CustomException(HttpStatus httpStatus, String errorMessage, Throwable throwable) {
        this.initCause(throwable);
        this.errorMessage = errorMessage;
        this.httpStatus = httpStatus;
    }

    protected CustomException(HttpStatus httpStatus, Throwable throwable) {
        this.initCause(throwable);
        this.httpStatus = httpStatus;
    }
}
