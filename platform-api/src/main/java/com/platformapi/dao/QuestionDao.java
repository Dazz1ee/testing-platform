package com.platformapi.dao;


import com.platformapi.models.Question;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface QuestionDao {
    Optional<Question> findById(Long id);

    void deleteById(Long id);

    Question save(Question question) throws SQLException;

    void saveAll(List<Question> question);
}
