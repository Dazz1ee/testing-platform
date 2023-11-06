package com.platformapi.dao;

import com.platformapi.models.Test;

public interface TestDao {
    Test findById(Long id);
    Test createTest(Test test);
    Test updateTest(Test test);
}
