package com.platformapi.services;

import com.platformapi.dao.*;
import com.platformapi.models.Question;
import com.platformapi.models.TestEntity;
import com.platformapi.models.TransmittingQuestionDto;
import com.platformapi.models.TransmittingTestDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class ListenerTest {
    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${api.kafka.adding-topic}")
    String addingTopic;

    @Value("${api.kafka.process-topic}")
    String processTopic;

    @Autowired
    TestDao testDaoImp;

    @Autowired
    QuestionDao questionDaoImp;

    @Autowired
    @Qualifier("customJdbcTemplate")
    JdbcTemplate jdbcTemplate;


    @BeforeAll
    void init() {

        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            String sql = "INSERT INTO users(email, password, first_name, second_name) " +
                    "VALUES ('test' , 'test', 'test', 'test')";
            jdbcTemplate.update(sql);

        } catch (SQLException sqlException) {

        }
    }

    @BeforeEach
    void clean() {
        jdbcTemplate.update("TRUNCATE TABLE tests CASCADE ");
        jdbcTemplate.update("TRUNCATE TABLE questions CASCADE ");
    }

    @Container
    static final KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:latest")
    );

    @Container
    static PostgreSQLContainer postgresqlContainer = new PostgreSQLContainer("postgres:15")
            .withDatabaseName("foo")
            .withUsername("foo")
            .withPassword("foo");

    static {
        postgresqlContainer.start();
        kafka.start();
    }


    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
    }

    @Test
    void addNewTest() {
        List<TransmittingQuestionDto> questionDtos = new ArrayList<>();
        questionDtos.add(new TransmittingQuestionDto(
                null, null, "test1",
                List.of("test1", "test2"), List.of("test2"))
        );

        questionDtos.add(new TransmittingQuestionDto(
                        null, null, "test2",
                        List.of("test1", "test2"), List.of("test1"))
        );

        TransmittingTestDto transmittingTestDto = TransmittingTestDto.builder()
                        .name("test")
                        .description("test")
                        .visible(true)
                        .issuerId(1L)
                        .duration(30)
                        .questions(questionDtos)
                        .build();

        kafkaTemplate.send(addingTopic, transmittingTestDto);

        await()
                .pollInterval(Duration.ofSeconds(3))
                .atMost(30, SECONDS)
                .untilAsserted(() -> {
                    Optional<TestEntity> testEntity = testDaoImp.findById(1L);
                    Optional<Question> question1 = questionDaoImp.findById(1L);
                    Optional<Question> question2 = questionDaoImp.findById(2L);

                    assertThat(testEntity).isPresent();
                    assertThat(question1).isPresent();
                    assertThat(question2).isPresent();
                });
    }

    @Test
    void addNewTestWithDeserializeException() {
        String notValid = ".";
        kafkaTemplate.send(addingTopic, notValid);
        await()
                .pollInterval(Duration.ofSeconds(3))
                .atMost(30, SECONDS)
                .untilAsserted(() -> {
                    Optional<TestEntity> testEntity = testDaoImp.findById(1L);

                    assertThat(testEntity).isEmpty();
                });
    }

    @Test
    void finishTest() {

    }
}