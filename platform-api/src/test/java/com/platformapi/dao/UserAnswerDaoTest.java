package com.platformapi.dao;

import com.platformapi.configurations.CustomJdbcTemplate;
import com.platformapi.exceptions.DuplicateAnswerException;
import com.platformapi.exceptions.SqlConnectionException;
import com.platformapi.mappers.UserAnswerMapper;
import com.platformapi.models.UserAnswer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@ActiveProfiles("test")
@EnableAutoConfiguration
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {UserAnswerDaoImp.class, CustomJdbcTemplate.class})
class UserAnswerDaoTest {
    @Autowired
    private UserAnswerDao userAnswerDao;

    @Autowired
    @Qualifier("customJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Container
    private static PostgreSQLContainer postgresqlContainer = new PostgreSQLContainer("postgres:16.1")
            .withDatabaseName("foo")
            .withUsername("foo")
            .withPassword("foo");

    static {
        postgresqlContainer.start();
    }

    @DynamicPropertySource
    static void setPropertySource(DynamicPropertyRegistry dynamicPropertySource) {
        dynamicPropertySource.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        dynamicPropertySource.add("spring.datasource.username", postgresqlContainer::getUsername);
        dynamicPropertySource.add("spring.datasource.password", postgresqlContainer::getPassword);
    }


    @BeforeEach
    void clearDB() {
        jdbcTemplate.update("TRUNCATE TABLE user_answers CASCADE ");
    }

    private Array createSqlArray(Connection connection, List<String> list) {
        try {
            return connection.createArrayOf("text", list.toArray());
        } catch (SQLException e) {
            throw new SqlConnectionException(e);
        }
    }

    @BeforeAll
    public void init() {

        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            simpleJdbcInsert.setTableName("questions");
            simpleJdbcInsert.setGeneratedKeyName("id");
            List<Long> ids = new ArrayList<>();
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("content", "test");
            parameters.put("options", createSqlArray(connection, List.of("test1", "test2", "answer")));
            parameters.put("correct_answer", createSqlArray(connection, List.of("answer")));


            for (int i = 0; i < 5; i++) {
                ids.add(simpleJdbcInsert.executeAndReturnKey(parameters).longValue());
            }

            String sql = "INSERT INTO users(email, password, first_name, second_name) " +
                    "VALUES ('test' , 'test', 'test', 'test')";
            jdbcTemplate.update(sql);

            parameters.clear();
            simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            simpleJdbcInsert.setTableName("tests");
            simpleJdbcInsert.setGeneratedKeyName("id");

            parameters.put("issuer_id", 1L);
            parameters.put("name", "test");
            parameters.put("description", "test");
            parameters.put("duration", 30);
            parameters.put("visible", "true");
            parameters.put("number_task", 5);
            Long testId = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();

            parameters.clear();
            simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            simpleJdbcInsert.setTableName("user_tests");
            simpleJdbcInsert.setGeneratedKeyName("id");

            parameters.put("user_id", 1L);
            parameters.put("test_id", 1L);
            parameters.put("started_time", Timestamp.from(Instant.now()));

            simpleJdbcInsert.executeAndReturnKey(parameters).longValue();

            sql = "INSERT INTO test_questions(question_id, test_id) VALUES (? , ?)";

            for (Long questionId : ids) {
                jdbcTemplate.update(sql, questionId, testId);
            }


        } catch (SQLException sqlException) {

        }
    }


    @Test
    void saveWhenNotExists() {
        UserAnswer userAnswer = UserAnswer.builder()
                .userTestId(1L)
                .taskId(1L)
                .answer(List.of("answer"))
                .correct(true)
                .build();
        userAnswerDao.save(userAnswer);

        int rowsInAnswerTable = jdbcTemplate
                .queryForObject("SELECT COUNT(task_id) FROM user_answers WHERE user_test_id = 1 AND task_id = 1", Integer.class);

        assertThat(rowsInAnswerTable).isEqualTo(1);
    }

    @Test
    void saveWhenExists() {
        UserAnswer userAnswer = UserAnswer.builder()
                .userTestId(1L)
                .taskId(1L)
                .answer(List.of("answer"))
                .correct(true)
                .build();
        userAnswerDao.save(userAnswer);

        assertThrows(DuplicateAnswerException.class, () -> userAnswerDao.save(userAnswer));
    }

    @Test
    void find() {
        UserAnswer userAnswer = UserAnswer.builder()
                .userTestId(1L)
                .taskId(3L)
                .answer(List.of("answer"))
                .correct(true)
                .build();
        userAnswerDao.save(userAnswer);

        Optional<UserAnswer> userAnswerReturned = userAnswerDao.find(1L, 3L);
        assertThat(userAnswerReturned).isPresent();
        assertThat(userAnswerReturned.get()).isEqualTo(userAnswer);
    }

    @Test
    void findWhenNotExists() {
        UserAnswer userAnswer = UserAnswer.builder()
                .userTestId(1L)
                .taskId(13L)
                .answer(List.of("answer"))
                .correct(true)
                .build();


        Optional<UserAnswer> userAnswerReturned = userAnswerDao.find(1L, 13L);
        assertThat(userAnswerReturned).isEmpty();
    }

    @Test
    void update() {
        UserAnswer userAnswer = UserAnswer.builder()
                .userTestId(1L)
                .taskId(3L)
                .answer(List.of("1"))
                .correct(false)
                .build();
        userAnswerDao.save(userAnswer);

        userAnswer.setCorrect(true);
        userAnswer.setAnswer(List.of("answer"));
        userAnswerDao.update(userAnswer);
        String sql = "SELECT * FROM user_answers WHERE user_test_id = ? and task_id = ?";
        UserAnswer newAnswer = jdbcTemplate.queryForObject(sql, new UserAnswerMapper(), userAnswer.getUserTestId(), userAnswer.getTaskId());

        assertThat(newAnswer).isEqualTo(userAnswer);
    }

    @Test
    void delete() {
        UserAnswer userAnswer = UserAnswer.builder()
                .userTestId(1L)
                .taskId(3L)
                .answer(List.of("1"))
                .correct(false)
                .build();
        userAnswerDao.save(userAnswer);

        userAnswerDao.delete(1L, 3L);
        String sql = "SELECT COUNT(task_id) FROM user_answers WHERE user_test_id = ? and task_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userAnswer.getUserTestId(), userAnswer.getTaskId());

        assertThat(count).isZero();
    }

}