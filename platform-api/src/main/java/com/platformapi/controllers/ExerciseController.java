package com.platformapi.controllers;

import com.platformapi.models.CustomPrincipal;
import com.platformapi.models.TransmittingTestDto;
import com.platformapi.services.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class ExerciseController {

    @Value("${api.kafka.adding-topic}")
    private String topicName;

    private final TestService testService;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    @GetMapping("/test/{id}")
    public ResponseEntity<?> getTestById(@PathVariable Long id) {
        return ResponseEntity.ok(testService.findById(id));
    }

    @GetMapping("/test")
    public ResponseEntity<?> getTestByUserId(@RequestParam(name = "user_id") Long id) {
        return ResponseEntity.ok(testService.findByUserId(id));
    }

    @PostMapping("/test")
    public ResponseEntity<?> createTest(@RequestBody TransmittingTestDto test,
                                        CustomPrincipal customPrincipal) {
        test.setIssuerId(customPrincipal.getId());
        kafkaTemplate.send(topicName, test);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/test")
    @PreAuthorize("#{testService.checkingOwnership(id, authentication.principal.id)}")
    public ResponseEntity<?> deleteTest(@RequestParam Long id) {
        testService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/test")
    @PreAuthorize("#{testService.checkingOwnership(id, authentication.principal.id)}")
    public ResponseEntity<?> updateTest(@RequestParam(required = false) Long id,
                                        @RequestBody TransmittingTestDto test,
                                        CustomPrincipal customPrincipal) {
        test.setIssuerId(customPrincipal.getId());
        testService.update(id, test);
        return ResponseEntity.noContent().build();
    }
}
