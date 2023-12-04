package com.platformapi.controllers;

import com.platformapi.exceptions.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class ExceptionController {

    @ExceptionHandler(CustomException.class)
    private ResponseEntity<?> customException(CustomException customException) {
        log.error(customException.getErrorMessage());

        if (customException.getHttpStatus().is5xxServerError()) {
            return ResponseEntity.status(customException.getHttpStatus()).build();
        }

        return ResponseEntity.status(customException.getHttpStatus()).body(customException.getErrorMessage());
    }
}
