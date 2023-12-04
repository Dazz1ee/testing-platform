package com.platformapi.exceptions;

import org.springframework.http.HttpStatus;

public class TestNotStartedException extends CustomException {
    public TestNotStartedException() {
        super(HttpStatus.BAD_REQUEST, "the user did not start the test");
    }
}
