package com.auth.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public abstract class CustomException extends RuntimeException {
    private final HttpStatusCode httpStatus;
    protected CustomException(HttpStatusCode httpStatus, String message, Throwable exceptions) {
        super(message, exceptions);
        this.httpStatus = httpStatus;
    }

    protected CustomException(HttpStatusCode httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

}
