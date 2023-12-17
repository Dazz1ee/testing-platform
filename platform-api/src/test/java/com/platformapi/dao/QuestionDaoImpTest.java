package com.platformapi.dao;

import com.platformapi.configurations.CustomJdbcTemplate;
import com.platformapi.models.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@Testcontainers
@ActiveProfiles("test")
@EnableAutoConfiguration
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {CustomJdbcTemplate.class, QuestionDaoImp.class})
class QuestionDaoImpTest {

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

    @Autowired
    @Qualifier("customJdbcTemplate")
    JdbcTemplate jdbcTemplate;

    @Autowired
    QuestionDao questionDao;

    @BeforeEach
    void clearDB() {
        jdbcTemplate.update("TRUNCATE TABLE test_questions CASCADE ");
        jdbcTemplate.update("TRUNCATE TABLE questions CASCADE ");
    }

    void saveTaskForTest() {
        jdbcTemplate.execute("INSERT INTO questions(id, content, options, correct_answer) " +
                "VALUES (1, 'Test', '{\"1\", \"2\"}', '{\"1\"}')");
    }

    @Test
    void save() throws SQLException {
        Question question = Question.builder()
                .content("test")
                .options(List.of("1", "2", "3", "4"))
                .correctAnswer(List.of("1", "2"))
                .build();

        questionDao.save(question);

        int actual = jdbcTemplate.queryForObject("SELECT COUNT(id) FROM questions WHERE id = ?", Integer.class, question.getId());

        assertThat(actual).isEqualTo(1);
    }

    @Test
    void saveWithException() {
        Question question = Question.builder()
                .options(List.of("1", "2", "3", "4"))
                .correctAnswer(List.of("1", "2"))
                .build();

        assertThrows(DataAccessException.class, () -> questionDao.save(question));
    }

    @Test
    void saveAll() {
        List<Question> questions = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Question question = Question.builder()
                    .content("test" + i)
                    .options(List.of("1", "2"))
                    .correctAnswer(List.of("1"))
                    .build();
            questions.add(question);
        }

        questionDao.saveAll(questions);

        int actual = jdbcTemplate.queryForObject("SELECT COUNT(id) FROM questions", Integer.class);
        assertThat(actual).isEqualTo(5);
    }

    @Test
    void findById() {
        Question expected = Question.builder()
                .id(1L)
                .content("Test")
                .options(List.of("1", "2"))
                .correctAnswer(List.of("1"))
                .build();
        saveTaskForTest();

        Optional<Question> actual = questionDao.findById(1L);

        assertThat(actual).isPresent().contains(expected);
    }

    @Test
    void notFindById() {
        Optional<Question> actual = questionDao.findById(333L);

        assertThat(actual).isEmpty();
    }

    @Test
    void deleteById() {
        saveTaskForTest();
        int countBefore = jdbcTemplate.queryForObject("SELECT COUNT(id) FROM questions WHERE id = 1", Integer.class);

        questionDao.deleteById(1L);
        int countAfter = jdbcTemplate.queryForObject("SELECT COUNT(id) FROM questions WHERE id = 1", Integer.class);

        assertThat(countBefore).isEqualTo(1);
        assertThat(countAfter).isZero();
    }
}
