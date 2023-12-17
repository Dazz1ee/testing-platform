package com.platformapi.models;

import lombok.Data;

import java.util.List;

@Data
public class TestAnswer {
    private Long testId;

    private Long userId;

    private List<TaskAnswer> taskAnswer;
}

