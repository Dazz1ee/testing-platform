package com.auth.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class GenerateRSAKeyException extends CustomException {
    public GenerateRSAKeyException(Exception ex) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error");
    }
}
