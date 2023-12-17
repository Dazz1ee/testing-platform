package com.platformapi.exceptions;

import org.springframework.http.HttpStatus;

public class WrongUserTestData extends CustomException {
    public WrongUserTestData(Throwable throwable) {
        super(HttpStatus.BAD_REQUEST, "Wrong test_id or user_id", throwable);
    }

    public WrongUserTestData() {
        super(HttpStatus.BAD_REQUEST, "Wrong test_id or user_id");
    }
}
