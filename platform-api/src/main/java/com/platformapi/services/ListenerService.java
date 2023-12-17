package com.platformapi.services;

import com.platformapi.dao.QuestionDao;
import com.platformapi.dao.TestDao;
import com.platformapi.dao.UserAnswerDao;
import com.platformapi.dao.UserTestDao;
import com.platformapi.exceptions.TestNotFoundException;
import com.platformapi.exceptions.TestNotStartedException;
import com.platformapi.models.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListenerService {
    private final UserTestDao userTestDao;
    private final TestDao testDao;
    private final QuestionDao questionDao;
    private final UserAnswerDao userAnswerDao;

    @KafkaListener(topics = "${api.kafka.adding-topic}",
                    containerFactory = "createTestListenerContainer")
    public void addNewTest(ConsumerRecord<String, TransmittingTestDto> data,
                           Acknowledgment acknowledgment) {
        TestEntity testEntity = TestEntity.builder()
                        .name(data.value().getName())
                        .description(data.value().getDescription())
                        .duration(data.value().getDuration())
                        .issuerId(data.value().getIssuerId())
                        .numberTask(data.value().getQuestions().size())
                        .visible(data.value().getVisible())
                        .questions(data.value().getQuestions().stream().map(questionDto ->
                            Question.builder()
                                    .id(questionDto.id())
                                    .correctAnswer(questionDto.correctAnswer())
                                    .image(questionDto.image())
                                    .content(questionDto.content())
                                    .options(questionDto.options())
                                    .build()

                        ).toList())
                        .build();

        questionDao.saveAll(testEntity.getQuestions());
        testDao.save(testEntity);

        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = "${api.kafka.process-topic}",
                    containerFactory = "kafkaListenerContainerFactory")
    public void finishTest(ConsumerRecord<String, TestAnswer> data, Acknowledgment acknowledgment) {
        TestAnswer testAnswer = data.value();

        TestEntity testEntity = testDao.findById(testAnswer.getTestId())
                .orElseThrow(TestNotFoundException::new);

        UserTest userTest = userTestDao
                .find(testAnswer.getUserId(), testAnswer.getTestId())
                .orElseThrow(() -> {
                    log.error("the user {} did not start the test {}", testAnswer.getUserId(), testAnswer.getTestId());
                    return new TestNotStartedException();
        });

        if (userTest.getFinishTime() != null) {
            log.error("The user {} already finished test {} on the {}", testAnswer.getUserId(), testAnswer.getTestId(), userTest.getFinishTime());
            throw new TestNotFoundException.TestAlreadyFinished();
        }

        if (userTest.getStartTime().plus(
                testEntity.getDuration() + 5, ChronoUnit.MINUTES)
                .isAfter(Instant.ofEpochMilli(data.timestamp()))) {

            log.error("The user {} failed to submit the task", testAnswer.getUserId());
            throw new TestNotFoundException.TestAlreadyFinished();
        }


        int countCorrect = 0;
        Map<Long, List<String>> correctAnswers = testEntity.getQuestions().stream()
                .collect(Collectors.toMap(Question::getId, Question::getCorrectAnswer));

        testAnswer.setTaskAnswer(
                distinct(testAnswer.getTaskAnswer().stream(), TaskAnswer::getTaskId).toList()
        );

        for (TaskAnswer answer : testAnswer.getTaskAnswer()) {
            UserAnswer userAnswer = UserAnswer.builder()
                    .userTestId(userTest.getId())
                    .answer(answer.getAnswer())
                    .taskId(answer.getTaskId())
                    .correct(false)
                    .build();

            if (answer.getAnswer().equals(correctAnswers.get(answer.getTaskId()))) {
                countCorrect++;
                userAnswer.setCorrect(true);
            }

            userAnswerDao.save(userAnswer);
        }

        userTest.setFinishTime(Instant.ofEpochMilli(data.timestamp()));
        userTest.setCountCorrect(countCorrect);
        userTestDao.update(userTest);

        acknowledgment.acknowledge();
    }

    private static <O, H> Stream<O> distinct(Stream<O> input, Function<O, H> keyExtractor) {
        Set<H> set = new HashSet<>();
        return input.filter(o -> set.add(keyExtractor.apply(o)));
    }

}
