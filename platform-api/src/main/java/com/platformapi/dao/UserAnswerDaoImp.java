package com.platformapi.dao;

import com.platformapi.exceptions.DuplicateAnswerException;
import com.platformapi.exceptions.SqlConnectionException;
import com.platformapi.mappers.UserAnswerMapper;
import com.platformapi.models.UserAnswer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class UserAnswerDaoImp implements UserAnswerDao {
    private final JdbcTemplate jdbcTemplate;

    public UserAnswerDaoImp(@Qualifier("customJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public void save(UserAnswer userAnswer) {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()){
            String sql = "INSERT INTO user_answers(user_test_id, task_id, answer, is_correct) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(sql, userAnswer.getUserTestId(), userAnswer.getTaskId(), createSqlArray(connection, userAnswer.getAnswer()), userAnswer.getCorrect());
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateAnswerException(ex);
        } catch (SQLException | NullPointerException exception) {
            throw new SqlConnectionException(exception);
        }
    }

    @Override
    public Optional<UserAnswer> find(Long userId, Long taskId) {
        String sql = "SELECT user_test_id, task_id, answer, is_correct FROM user_answers WHERE user_test_id = ? AND task_id = ?";
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, new UserAnswerMapper(), userId, taskId));
    }

    @Override
    public UserAnswer update(UserAnswer userAnswer) {
        String sql = "UPDATE user_answers SET is_correct = ?, answer = ? WHERE user_test_id = ? AND task_id = ?";
        try(Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            jdbcTemplate.update(sql,
                    userAnswer.getCorrect(), createSqlArray(connection, userAnswer.getAnswer()),
                    userAnswer.getUserTestId(), userAnswer.getTaskId());

            return userAnswer;
        } catch (SQLException dataAccessException) {
            throw new SqlConnectionException(dataAccessException);
        }

    }

    @Override
    public void delete(Long userId, Long taskId) {
        String sql = "DELETE FROM user_answers WHERE task_id = ? and user_test_id = ?";
        jdbcTemplate.update(sql, taskId, userId);
    }

    private Array createSqlArray(Connection connection, List<String> list) {
        try {
            return connection.createArrayOf("text", list.toArray());
        } catch (SQLException e) {
            throw new SqlConnectionException(e);
        }
    }

}
