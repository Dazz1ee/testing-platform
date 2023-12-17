package com.platformapi.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

public record TransmittingQuestionDto(
        Long id,
        UUID image,
        String content,
        List<String> options,
        List<String> correctAnswer
){
    public TransmittingQuestionDto(Question question) {
        this(question.getId(), question.getImage(), question.getContent(),
                question.getOptions(), question.getCorrectAnswer());
    }


}
