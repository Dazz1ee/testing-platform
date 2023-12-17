package com.platformapi.exceptions;

import org.springframework.http.HttpStatus;

public class TestAlreadyStarted extends CustomException {
    public TestAlreadyStarted(Throwable throwable) {
        super(HttpStatus.CONFLICT, "The test has already started", throwable);
    }
}
