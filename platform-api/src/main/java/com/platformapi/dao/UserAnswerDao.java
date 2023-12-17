package com.platformapi.dao;

import com.platformapi.models.UserAnswer;

import java.util.Optional;

public interface UserAnswerDao {
    void save(UserAnswer userTest);

    Optional<UserAnswer> find(Long userId, Long taskId);
    UserAnswer update(UserAnswer userTest);

    void delete(Long userId, Long taskId);
}
