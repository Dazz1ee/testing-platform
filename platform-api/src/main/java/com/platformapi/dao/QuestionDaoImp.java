package com.platformapi.dao;

import com.platformapi.mappers.QuestionMapper;
import com.platformapi.models.Question;
import com.platformapi.exceptions.SqlConnectionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

@Repository
@Transactional(isolation = Isolation.READ_COMMITTED)
@Slf4j
public class QuestionDaoImp implements QuestionDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public QuestionDaoImp(@Qualifier("customJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("questions")
                .usingGeneratedKeyColumns("id");
    }
    @Override
    public Optional<Question> findById(Long id) {
        String query = "SELECT id, content, options, correct_answer, image FROM questions WHERE id = ?";
        return Optional.ofNullable(jdbcTemplate.queryForObject(query,
                new QuestionMapper(),
                id));
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM test_questions WHERE id = ?", id);
        jdbcTemplate.update("DELETE FROM questions WHERE id = ?", id);
    }

    @Override
    public Question save(Question question) {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("content", question.getContent());
            parameters.put("options", createSqlArray(connection, question.getOptions()));
            parameters.put("correct_answer", createSqlArray(connection, question.getCorrectAnswer()));
            parameters.put("image", question.getImage());
            long id = jdbcInsert.executeAndReturnKey(parameters).intValue();
            question.setId(id);
            return question;
        } catch (SQLException e) {
            throw new SqlConnectionException(e);
        }
    }

    @Override
    public void saveAll(List<Question> questions) {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            for (Question question : questions) {
                if (question.getId() == null || question.getId() == 0) {
                    Map<String, Object> parameters = new HashMap<>();
                    parameters.put("content", question.getContent());
                    parameters.put("options", createSqlArray(connection, question.getOptions()));
                    parameters.put("correct_answer", createSqlArray(connection, question.getCorrectAnswer()));
                    parameters.put("image", question.getImage());

                    long id = jdbcInsert.executeAndReturnKey(parameters).intValue();
                    question.setId(id);
                }
            }
        } catch (SQLException | NullPointerException e) {
            throw new SqlConnectionException(e);
        }
    }

    private Array createSqlArray(Connection connection, List<String> list) {
        if (list == null) {
            return null;
        }
        try {
            return connection.createArrayOf("text", list.toArray());
        } catch (SQLException e) {
            throw new SqlConnectionException(e);
        }
    }

}
