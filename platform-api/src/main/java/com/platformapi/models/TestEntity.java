package com.platformapi.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class TestEntity {

    private Long id;

    private Long issuerId;

    private String name;

    private String description;

    private Integer duration;

    private Boolean visible;

    private Integer numberTask;

    private List<Question> questions;

    public TestEntity() {
        questions = new ArrayList<>();
    }

}
