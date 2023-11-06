package com.platformapi.controllers;

import jakarta.annotation.security.PermitAll;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class Test {
    @GetMapping("/test")
    @PermitAll
    public ResponseEntity<?> test(Authentication authentication) {
        return ResponseEntity.ok(authentication.getPrincipal());
    }
}
