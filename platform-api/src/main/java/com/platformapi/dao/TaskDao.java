package com.platformapi.dao;


import com.platformapi.models.Task;

public interface TaskDao {
    Task findById(Long id);
    Task findByTestId(Long id);
    
}
