package com.platformapi.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransmittingTestDto {
        private Long issuerId;

        private String name;

        private String description;

        private Integer duration;

        private Boolean visible;

        private List<TransmittingQuestionDto> questions;

        public TransmittingTestDto(TestEntity testEntity) {
                issuerId = testEntity.getId();
                name = testEntity.getName();
                description = testEntity.getDescription();
                duration = testEntity.getDuration();
                visible = testEntity.getVisible();
                questions = testEntity.getQuestions().stream().map(TransmittingQuestionDto::new).toList();
        }
}
