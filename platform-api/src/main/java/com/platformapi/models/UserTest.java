package com.platformapi.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTest {
    private Long id;

    private Long testId;

    private Long userId;

    private Instant startTime;

    private Instant finishTime;

    private Integer countCorrect;

    private List<UserAnswer> questions;
}
