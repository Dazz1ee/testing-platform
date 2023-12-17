package com.platformapi.dao;

import com.platformapi.models.UserTest;

import java.util.Optional;

public interface UserTestDao {
    Long save(UserTest userTest);

    Optional<UserTest> find(Long userId, Long testId);

    Optional<UserTest> findWithTasks(Long userId, Long testId);

    Optional<UserTest> findById(Long id);

    UserTest update(UserTest userTest);

    void delete(Long userId, Long testId);

}
