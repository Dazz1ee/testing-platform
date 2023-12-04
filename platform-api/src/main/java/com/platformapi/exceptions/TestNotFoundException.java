package com.platformapi.exceptions;

import org.springframework.http.HttpStatus;

public class TestNotFoundException extends CustomException {
    public TestNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Not found");
    }

    public static class TestAlreadyFinished extends CustomException {
        public TestAlreadyFinished() {
            super(HttpStatus.BAD_REQUEST, "The user already finished test");
        }
    }
}
