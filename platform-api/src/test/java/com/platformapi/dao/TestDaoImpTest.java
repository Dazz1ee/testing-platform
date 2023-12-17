package com.platformapi.dao;

import com.platformapi.configurations.CustomJdbcTemplate;
import com.platformapi.models.Question;
import com.platformapi.models.TestEntity;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@Testcontainers
@ActiveProfiles("test")
@EnableAutoConfiguration
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {CustomJdbcTemplate.class, TestDaoImp.class})
class TestDaoImpTest {
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
    TestDao testDao;

    @BeforeEach
    void clearDB() {
        jdbcTemplate.update("TRUNCATE TABLE test_questions RESTART IDENTITY CASCADE");
        jdbcTemplate.update("TRUNCATE TABLE questions RESTART IDENTITY CASCADE");
        jdbcTemplate.update("TRUNCATE TABLE tests RESTART IDENTITY CASCADE ");
    }


    TestEntity createTestDto() {
        List<Question> listQuestions = new ArrayList<>();
        listQuestions.add(Question.builder()
                .content("test")
                .id(1L)
                .build());

        listQuestions.add(Question.builder()
                .content("test")
                .id(2L)
                .build());

        listQuestions.add(Question.builder()
                .content("test")
                .id(3L)
                .build());

        jdbcTemplate.update("INSERT INTO questions(content) VALUES (?), (?), (?)",
                 "test", "test", "test");

        return TestEntity.builder()
                .id(1L)
                .description("test")
                .name("test")
                .questions(listQuestions)
                .numberTask(3)
                .issuerId(1L)
                .visible(true)
                .build();

    }

    TestEntity saveTestDto(Long id) {
        TestEntity testEntity = createTestDto();

        jdbcTemplate.update("INSERT INTO tests(name ,description, duration, number_task, issuer_id) VALUES (?, ?, ?, ?, ?)",
                testEntity.getName(), testEntity.getDescription(),
                testEntity.getDuration(), testEntity.getNumberTask(),
                testEntity.getIssuerId());

        jdbcTemplate.update("INSERT INTO test_questions(test_id, question_id) VALUES (?, ?), (?, ?), (?, ?)",
                id, 1, id, 2, id, 3);

        return testEntity;
    }
    @Test
    void save() {
        TestEntity testEntity = createTestDto();

        testDao.save(testEntity);

        Integer actual1 = jdbcTemplate.queryForObject("SELECT COUNT(id) FROM tests", Integer.class);
        Integer actual2 = jdbcTemplate.queryForObject("SELECT COUNT(id) FROM test_questions", Integer.class);

        assertThat(actual1).isEqualTo(1L);
        assertThat(actual2).isEqualTo(3L);
    }
    @Test
    void findById() {
        TestEntity testEntity = saveTestDto(1L);

        Optional<TestEntity> actual = testDao.findById(1L);
        assertThat(actual.get()).isEqualTo(testEntity);
    }
    @Test
    void findByIdWhenNotExists() {
        Optional<TestEntity> actual = testDao.findById(1L);
        assertThat(actual).isEmpty();
    }
    @Test
    void findByIdWithoutTask() {
        TestEntity testEntity1 = saveTestDto(1L);
        TestEntity testEntity2 = saveTestDto(2L);

        testEntity1.setQuestions(new ArrayList<>());
        testEntity2.setQuestions(new ArrayList<>());
        List<TestEntity> actual = testDao.findByUserIdWithoutTask(1L);
        assertThat(actual).hasSize(2);

        assertThat(actual).contains(testEntity1);
        assertThat(actual).contains(testEntity2);
    }

    @Test
    void findByIdWithoutTaskWhenNotExists() {
        List<TestEntity> actual = testDao.findByUserIdWithoutTask(1L);

        assertThat(actual).isEmpty();
    }

    @Test
    void updateWhenTasksLess() {
        TestEntity testEntity = saveTestDto(1L);
        testEntity.getQuestions().remove(2);

        int count = jdbcTemplate.queryForObject("SELECT COUNT(id) FROM test_questions WHERE test_id = 1", Integer.class);
        assertThat(count).isEqualTo(3);
        testDao.update(testEntity);

        count = jdbcTemplate.queryForObject("SELECT COUNT(id) FROM test_questions WHERE test_id = 1", Integer.class);
        assertThat(count).isEqualTo(2);
    }
    @Test
    void updateWhenTasksAdded() {
        TestEntity testEntity = saveTestDto(1L);
        testEntity.getQuestions().add(Question.builder().content("test").build());

        int count = jdbcTemplate.queryForObject("SELECT COUNT(id) FROM test_questions WHERE test_id = 1", Integer.class);
        assertThat(count).isEqualTo(3);
        testDao.update(testEntity);

        count = jdbcTemplate.queryForObject("SELECT COUNT(id) FROM test_questions WHERE test_id = 1", Integer.class);
        assertThat(count).isEqualTo(4);
    }
    @Test
    void update() {
        TestEntity testEntity = saveTestDto(1L);
        testEntity.getQuestions().remove(2);
        testEntity.getQuestions().remove(1);
        testEntity.getQuestions().add(Question.builder().content("test").build());

        int count = jdbcTemplate.queryForObject("SELECT COUNT(id) FROM test_questions WHERE test_id = 1", Integer.class);
        assertThat(count).isEqualTo(3);
        testDao.update(testEntity);

        count = jdbcTemplate.queryForObject("SELECT COUNT(id) FROM test_questions WHERE test_id = 1", Integer.class);
        assertThat(count).isEqualTo(2);
    }
    @Test
    void updateWhenTestNotExists() {
        TestEntity testEntity = createTestDto();
        assertThrows(DataAccessException.class, () -> testDao.update(testEntity));


    }

    @Test
    void deleteById() {
        saveTestDto(1L);

        int numberTest = jdbcTemplate.queryForObject("SELECT COUNT(id) FROM tests WHERE id = 1", Integer.class);
        int numberTestQuestions = jdbcTemplate.queryForObject("SELECT COUNT(id) FROM test_questions WHERE test_id = 1", Integer.class);
        assertThat(numberTest).isEqualTo(1);
        assertThat(numberTestQuestions).isEqualTo(3);

        testDao.deleteById(1L);

        numberTest = jdbcTemplate.queryForObject("SELECT COUNT(id) FROM tests WHERE id = 1", Integer.class);
        numberTestQuestions = jdbcTemplate.queryForObject("SELECT COUNT(id) FROM test_questions WHERE test_id = 1", Integer.class);

        assertThat(numberTest).isZero();
        assertThat(numberTestQuestions).isZero();
    }
    @Test
    void deleteByIdWhenNotExists() {
        saveTestDto(1L);
        int numberTestBefore = jdbcTemplate.queryForObject("SELECT COUNT(id) FROM tests WHERE id = 666", Integer.class);
        testDao.deleteById(666L);
        int numberTestAfter = jdbcTemplate.queryForObject("SELECT COUNT(id) FROM tests WHERE id = 666", Integer.class);

        assertThat(numberTestBefore).isEqualTo(numberTestAfter);
    }
    @Test
    void getIssuerId() {
        TestEntity testEntity = saveTestDto(1L);
        Long issuerId = testDao.getIssuerId(testEntity.getId());

        assertThat(issuerId).isEqualTo(testEntity.getIssuerId());
    }
    @Test
    void getIssuerIdWhenTestNotFounded() {
        TestEntity testEntity = saveTestDto(1L);
        Long issuerId = testDao.getIssuerId(testEntity.getId());

        assertThat(issuerId).isEqualTo(testEntity.getIssuerId());
    }
}