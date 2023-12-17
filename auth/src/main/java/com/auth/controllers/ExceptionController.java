package com.auth.controllers;

import com.auth.exceptions.CustomException;
import com.auth.models.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class ExceptionController {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponseDto> response(CustomException exception) {
        log.debug("{}", exception.getMessage(), exception);
        return ResponseEntity.status(exception.getHttpStatus()).body(new ErrorResponseDto(exception.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDto> response(AuthenticationException exception) {
        log.debug("AuthenticationException", exception);
        return ResponseEntity.status(401).body(new ErrorResponseDto(exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Void> response(Exception exception) {
        log.debug("", exception);
        return ResponseEntity.internalServerError().build();
    }

}
