package com.platformapi.dao;

import com.platformapi.models.TestEntity;

import java.util.List;
import java.util.Optional;

public interface TestDao {
    List<TestEntity> findByUserIdWithoutTask(Long id);
    Optional<TestEntity> findById(Long id);
    TestEntity save(TestEntity testEntity);
    TestEntity update(TestEntity testEntity);
    void deleteById(Long id);
    Long getIssuerId(Long testId);
    Optional<Boolean> isVisible(Long testId);
}
