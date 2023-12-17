package com.auth.exceptions;

import org.springframework.http.HttpStatus;

public class DuplicateEmailException extends CustomException {
    public DuplicateEmailException() {
        super(HttpStatus.BAD_REQUEST, "Email is already in use");
    }
}
