package com.platformapi.models;

import lombok.Data;

import java.util.List;

@Data
public class TaskAnswer {
    private Long taskId;

    private List<String> answer;

}
