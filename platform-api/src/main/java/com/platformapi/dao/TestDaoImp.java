package com.platformapi.dao;

import com.platformapi.mappers.TestMapper;
import com.platformapi.models.Question;
import com.platformapi.models.TestEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class TestDaoImp implements TestDao {
    private final JdbcTemplate jdbcTemplate;


    public TestDaoImp(@Qualifier("customJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<TestEntity> findById(Long id) {
        String query = "SELECT tests.id, tests.name, tests.description, tests.issuer_id, " +
                "tests.visible, tests.number_task, tests.duration, questions.id AS question_id, questions.content, " +
                "questions.options, questions.image, questions.correct_answer " +
                "FROM tests JOIN test_questions ON tests.id = test_questions.test_id " +
                "JOIN questions ON test_questions.question_id = questions.id WHERE tests.id = ?";
        return Optional.ofNullable(jdbcTemplate.query(query, new TestMapper(), id));
    }

    @Override
    public List<TestEntity> findByUserIdWithoutTask(Long id) {
        String query = "SELECT id, name, description, " +
                "issuer_id, visible,number_task, duration FROM tests WHERE issuer_id = ?";
        return jdbcTemplate.query(query, BeanPropertyRowMapper.newInstance(TestEntity.class), id);
    }

    @Override
    public TestEntity save(TestEntity testEntity) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("tests")
                .usingGeneratedKeyColumns("id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("description", testEntity.getDescription());
        parameters.put("duration", testEntity.getDuration());
        parameters.put("name", testEntity.getName());
        parameters.put("visible", testEntity.getVisible());
        parameters.put("number_task", testEntity.getNumberTask());
        parameters.put("issuer_id", testEntity.getIssuerId());

        Long id = jdbcInsert.executeAndReturnKey(parameters).longValue();
        testEntity.setId(id);

        for (Question question : testEntity.getQuestions()) {
            jdbcTemplate.update("INSERT INTO test_questions(question_id, test_id) VALUES (?, ?)", question.getId(), testEntity.getId());
        }

        return testEntity;
    }

    @Override
    public TestEntity update(TestEntity testEntity) {
        SimpleJdbcInsert jdbcInsertTask = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("questions")
                .usingGeneratedKeyColumns("id");

        String sql = "SELECT question_id FROM test_questions WHERE test_id = ? FOR UPDATE";
        List<Long> oldTaskId =  jdbcTemplate.queryForList(sql, Long.class, testEntity.getId());
        Map<Long, Boolean> newTaskIdContainer = new HashMap<>();
        for (Question question : testEntity.getQuestions()) {
            if (question.getId() == null || question.getId() == 0) {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("content", question.getContent());
                parameters.put("options", question.getOptions());
                parameters.put("correct_answer", question.getCorrectAnswer());
                parameters.put("image", question.getImage());
                question.setId(jdbcInsertTask.executeAndReturnKey(parameters).longValue());
            }

            if (!oldTaskId.contains(question.getId())) {
                jdbcTemplate.update("INSERT INTO test_questions(question_id, test_id) VALUES  (?, ?)", question.getId(), testEntity.getId());
            }

            newTaskIdContainer.put(question.getId(), true);
        }

        oldTaskId.stream().filter(id -> !newTaskIdContainer.containsKey(id)).forEach(id ->
                jdbcTemplate.update("DELETE FROM test_questions WHERE question_id = ? and test_id = ?", id, testEntity.getId())
        );

        jdbcTemplate.update("UPDATE tests SET description = ?, " +
                "name = ?, visible = ?, number_task = ? WHERE id = ?",
                testEntity.getDescription(), testEntity.getName(), testEntity.getVisible(),
                testEntity.getNumberTask(), testEntity.getId()
        );

        return testEntity;
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate
                .queryForList("SELECT id FROM test_questions WHERE test_id = ?", Integer.class, id)
                .forEach(taskId ->
                            jdbcTemplate.update("DELETE FROM user_answers WHERE task_id = ?", taskId)
                );

        jdbcTemplate.update("DELETE FROM test_questions WHERE test_id = ?", id);
        jdbcTemplate.update("DELETE FROM tests WHERE id = ?", id);
    }

    @Override
    public Long getIssuerId(Long testId) {
        return jdbcTemplate.queryForObject(
                "SELECT issuer_id FROM tests WHERE id = ?", Long.class, testId);
    }

    @Override
    public Optional<Boolean> isVisible(Long testId) {
        String sql = "SELECT visible FROM tests WHERE id = ?";
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql,
                Boolean.class,
                testId));
    }
}
