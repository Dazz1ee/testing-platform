package com.platformapi.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswer {
    private Long userTestId;

    private Long taskId;

    private List<String> answer;

    private Boolean correct;
}
