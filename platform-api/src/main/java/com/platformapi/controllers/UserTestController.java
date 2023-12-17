package com.platformapi.controllers;

import com.platformapi.models.*;
import com.platformapi.services.UserTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("")
public class UserTestController {
    @Value("${api.kafka.process-topic}")
    private String finishTopic;

    private final UserTestService userTestService;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping("/test")
    public ResponseEntity<?> startTest(@RequestBody TestStartingDto testDto,
                                        CustomPrincipal customPrincipal) {
        userTestService.startTest(testDto, customPrincipal.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/user-finish-test")
    public ResponseEntity<?> finishTest(@RequestBody TestAnswer testAnswer,
                                        CustomPrincipal customPrincipal) {
        kafkaTemplate.send(finishTopic, T);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/user-test-info")
    @PreAuthorize("#{!(userTestService.isVisible(testId))}")
    public ResponseEntity<?> info(@RequestParam("id") Long testId, CustomPrincipal customPrincipal) {
        return ResponseEntity.ok().body(userTestService.getResult(testId, customPrincipal.getId()));
    }

}
