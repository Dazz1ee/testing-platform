package com.platformapi.dao;

import com.platformapi.exceptions.UserTestIntegrityViolationException;
import com.platformapi.mappers.UserTestMapper;
import com.platformapi.models.UserAnswer;
import com.platformapi.models.UserTest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.*;

@Repository
public class UserTestDaoImp implements UserTestDao {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public UserTestDaoImp(@Qualifier("customJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        simpleJdbcInsert.setTableName("user_tests");
        simpleJdbcInsert.setGeneratedKeyName("id");
    }

    @Override
    public Long save(UserTest userTest) {
        Map<String, Object> props = new HashMap<>();
        props.put("test_id", userTest.getTestId());
        props.put("user_id", userTest.getUserId());
        props.put("started_time", Timestamp.from(userTest.getStartTime()));

        try {
            Long id = simpleJdbcInsert.executeAndReturnKey(props).longValue();
            userTest.setId(id);
            return id;
        } catch (DataIntegrityViolationException ex) {
            throw new UserTestIntegrityViolationException(ex);
        }
    }

    @Override
    public Optional<UserTest> find(Long userId, Long testId) {
        String sql = "SELECT id, test_id, user_id, started_time, completed_time, count_correct " +
                "FROM user_tests WHERE user_id = ? and test_id = ?";
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, new UserTestMapper(), userId, testId));
    }

    @Override
    public Optional<UserTest> findWithTasks(Long userId, Long testId) {
        String sql = "SELECT id, test_id, user_id, started_time, answer, " +
                "completed_time, count_correct, task_id, is_correct " +
                "FROM user_tests JOIN user_answers ON user_tests.id = user_answers.user_test_id " +
                "WHERE user_tests.user_id = ? and user_tests.test_id = ?";
        return Optional.ofNullable(jdbcTemplate.query(sql,(ResultSet resultSet) -> {
            UserTest userTest = null;
            if (resultSet.next()) {
                userTest = UserTest.builder()
                        .id(resultSet.getLong("id"))
                        .userId(resultSet.getLong("user_id"))
                        .testId(resultSet.getLong("test_id"))
                        .startTime(resultSet.getTimestamp("started_time") != null ?
                                resultSet.getTimestamp("started_time").toInstant() :
                                null
                        )
                        .finishTime(resultSet.getTimestamp("completed_time") != null ?
                                resultSet.getTimestamp("completed_time").toInstant() :
                                null
                        )
                        .countCorrect(resultSet.getObject("count_correct", Integer.class))
                        .questions(new ArrayList<>())
                        .build();

                userTest.getQuestions().add(
                        UserAnswer.builder()
                                .userTestId(resultSet.getLong("id"))
                                .taskId(resultSet.getLong("task_id"))
                                .correct(resultSet.getObject("is_correct", Boolean.class))
                                .answer(Arrays.stream((String[]) resultSet.getArray("answer").getArray())
                                        .toList())
                                .build()
                );

                while (resultSet.next()) {
                    userTest.getQuestions().add(
                            UserAnswer.builder()
                                    .userTestId(resultSet.getLong("id"))
                                    .taskId(resultSet.getLong("task_id"))
                                    .correct(resultSet.getObject("is_correct", Boolean.class))
                                    .answer(Arrays.stream((String[]) resultSet.getArray("answer").getArray())
                                            .toList())
                                    .build());
                }
            }

            return userTest;
        }, userId, testId));
    }

    @Override
    public Optional<UserTest> findById(Long id) {
        String sql = "SELECT id, test_id, user_id, started_time, completed_time, count_correct " +
                "FROM user_tests WHERE id = ?";
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, new UserTestMapper(), id));
    }

    @Override
    public UserTest update(UserTest userTest) {
        String sql = "UPDATE user_tests SET count_correct = ?, completed_time = ? WHERE test_id = ? AND user_id = ?";
        jdbcTemplate.update(sql,
                userTest.getCountCorrect(),
                userTest.getFinishTime() != null ?
                        Timestamp.from(userTest.getFinishTime()) :
                        null,
                userTest.getTestId(),
                userTest.getUserId());

        return userTest;
    }

    @Override
    public void delete(Long userId, Long testId) {
        String sql = "DELETE FROM user_tests WHERE test_id = ? and user_id = ?";
        jdbcTemplate.update(sql, testId, userId);
    }

}
