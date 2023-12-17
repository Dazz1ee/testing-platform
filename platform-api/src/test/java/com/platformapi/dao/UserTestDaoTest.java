package com.platformapi.dao;

import com.platformapi.configurations.CustomJdbcTemplate;
import com.platformapi.exceptions.SqlConnectionException;
import com.platformapi.exceptions.UserTestIntegrityViolationException;
import com.platformapi.mappers.UserTestMapper;
import com.platformapi.models.UserAnswer;
import com.platformapi.models.UserTest;
import org.junit.jupiter.api.*;
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@ActiveProfiles("test")
@EnableAutoConfiguration
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {UserTestDaoImp.class, CustomJdbcTemplate.class})
class UserTestDaoTest {
    @Autowired
    private UserTestDao userTestDao;

    @Autowired
    @Qualifier("customJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    private Array createSqlArray(Connection connection, List<String> list) {
        try {
            return connection.createArrayOf("text", list.toArray());
        } catch (SQLException e) {
            throw new SqlConnectionException(e);
        }
    }

    @Container
    private static PostgreSQLContainer postgresqlContainer = new PostgreSQLContainer("postgres:16.1")
            .withDatabaseName("foo")
            .withUsername("foo")
            .withPassword("secret");

    static {
        postgresqlContainer.start();
    }

    @DynamicPropertySource
    static void setPropertySource(DynamicPropertyRegistry dynamicPropertySource) {
        dynamicPropertySource.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        dynamicPropertySource.add("spring.datasource.username", postgresqlContainer::getUsername);
        dynamicPropertySource.add("spring.datasource.password", postgresqlContainer::getPassword);
    }

    @Test
    void test() {
        assertThat(postgresqlContainer.isRunning()).isTrue();
        assertThat(postgresqlContainer.isRunning()).isTrue();
    }


    @BeforeEach
    void clearDB() {
        jdbcTemplate.update("TRUNCATE TABLE user_tests CASCADE");
    }

    @BeforeAll
    public void init() {
        jdbcTemplate.update("TRUNCATE TABLE user_answers CASCADE");
        jdbcTemplate.update("TRUNCATE TABLE user_tests CASCADE");

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

            sql = "INSERT INTO test_questions(question_id, test_id) VALUES (? , ?)";

            for (Long questionId : ids) {
                jdbcTemplate.update(sql, questionId, testId);
            }

        } catch (SQLException sqlException) {

        }
    }

    @Test
    void saveWhenNotExists() {
        UserTest userTest = UserTest.builder()
                .userId(1L)
                .testId(1L)
                .startTime(Instant.now().truncatedTo(ChronoUnit.MINUTES))
                .build();
        userTestDao.save(userTest);

        int rowsInAnswerTable = jdbcTemplate
                .queryForObject("SELECT COUNT(test_id) FROM user_tests WHERE test_id = 1 AND user_id = 1", Integer.class);

        assertThat(rowsInAnswerTable).isEqualTo(1);
    }

    @Test
    void saveWhenTestNotExists() {
        UserTest userTest = UserTest.builder()
                .userId(1L)
                .testId(133L)
                .startTime(Instant.now().truncatedTo(ChronoUnit.MINUTES))
                .build();

        assertThrows(UserTestIntegrityViolationException.class, () -> userTestDao.save(userTest));
    }


    @Test
    void saveWhenUserNotExists() {
        UserTest userTest = UserTest.builder()
                .userId(133L)
                .testId(1L)
                .startTime(Instant.now().truncatedTo(ChronoUnit.MINUTES))
                .build();

        assertThrows(UserTestIntegrityViolationException.class, () -> userTestDao.save(userTest));
    }

    @Test
    void saveWhenExists() {
        UserTest userTest = UserTest.builder()
                .userId(1L)
                .testId(1L)
                .startTime(Instant.now().truncatedTo(ChronoUnit.MINUTES))
                .build();
        userTestDao.save(userTest);

        assertThrows(UserTestIntegrityViolationException.class, () -> userTestDao.save(userTest));
    }

    @Test
    void find() {
        UserTest userTest = UserTest.builder()
                .userId(1L)
                .testId(1L)
                .startTime(Instant.now().truncatedTo(ChronoUnit.MINUTES))
                .build();
        userTestDao.save(userTest);

        Optional<UserTest> userTestReturned = userTestDao.find(1L, 1L);
        assertThat(userTestReturned).isPresent();
        assertThat(userTestReturned.get()).isEqualTo(userTest);
    }

    @Test
    void findById() {
        UserTest userTest = UserTest.builder()
                .userId(1L)
                .testId(1L)
                .startTime(Instant.now().truncatedTo(ChronoUnit.MINUTES))
                .build();
        userTestDao.save(userTest);

        Optional<UserTest> userTestReturned = userTestDao.findById(userTest.getId());
        assertThat(userTestReturned).isPresent();
        assertThat(userTestReturned.get()).isEqualTo(userTest);
    }

    @Test
    void findWithTasks() throws SQLException {
        UserTest userTest = UserTest.builder()
                .userId(1L)
                .testId(1L)
                .startTime(Instant.now().truncatedTo(ChronoUnit.MINUTES))
                .questions(new ArrayList<>())
                .build();
        userTestDao.save(userTest);
        List<UserAnswer> userAnswers = List.of(
                new UserAnswer(userTest.getId(), 1L, List.of("test1", "test2"), true),
                new UserAnswer(userTest.getId(), 2L, List.of("test1", "test2"), true),
                new UserAnswer(userTest.getId(), 3L, List.of("test1", "test2"), true)
        );
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            for (int i = 1; i < 4; i++) {
                jdbcTemplate.update("INSERT INTO user_answers(user_test_id, task_id, answer, is_correct) " +
                        "VALUES (?, ?, ?, true)", userTest.getId(), i, createSqlArray(connection, List.of("test1", "test2")));
            }
        }

        Optional<UserTest> userTestReturned = userTestDao.findWithTasks(userTest.getUserId(),
                                                                        userTest.getTestId());
        assertThat(userTestReturned).isPresent();
        assertThat(userTestReturned.get().getQuestions()).containsExactlyElementsOf(userAnswers);
    }


    @Test
    void findByIdWhenNotExists() {
        UserTest userTest = UserTest.builder()
                .userId(1L)
                .testId(1L)
                .startTime(Instant.now().truncatedTo(ChronoUnit.MINUTES))
                .build();
        userTestDao.save(userTest);

        Optional<UserTest> userTestReturned = userTestDao.findById(133L);
        assertThat(userTestReturned).isEmpty();
    }

    @Test
    void findWhenNotExists() {
        Optional<UserTest> userTestReturned = userTestDao.find(133L, 133L);
        assertThat(userTestReturned).isEmpty();
    }

    @Test
    void update() {
        UserTest userTest = UserTest.builder()
                .testId(1L)
                .userId(1L)
                .startTime(Instant.now().truncatedTo(ChronoUnit.MINUTES))
                .build();
        userTestDao.save(userTest);

        userTest.setCountCorrect(4);
        userTestDao.update(userTest);
        String sql = "SELECT * FROM user_tests WHERE user_id = ? AND test_id = ?";

        UserTest newAnswer = jdbcTemplate.queryForObject(sql, new UserTestMapper(), userTest.getUserId(), userTest.getTestId());
        assertThat(userTest).isEqualTo(newAnswer);
    }

    @Test
    void delete() {
        UserTest userTest = UserTest.builder()
                .testId(1L)
                .userId(1L)
                .startTime(Instant.now().truncatedTo(ChronoUnit.MINUTES))
                .build();
        userTestDao.save(userTest);

        userTestDao.delete(1L, 1L);
        String sql = "SELECT COUNT(test_id) FROM user_tests WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userTest.getId());

        assertThat(count).isZero();
    }
}