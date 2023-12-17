package com.platformapi.exceptions;

import org.springframework.http.HttpStatus;

public class SqlConnectionException extends CustomException{
    public SqlConnectionException(Throwable throwable) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, throwable);
    }
}
