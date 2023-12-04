package com.platformapi.services;

import com.platformapi.dao.QuestionDao;
import com.platformapi.dao.TestDao;
import com.platformapi.exceptions.TestNotFoundException;
import com.platformapi.models.Question;
import com.platformapi.models.TestEntity;
import com.platformapi.models.TransmittingTestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
/* TODO
 *   TestService
 *       Добавление нового теста ---
 *       Удаление теста ---
 *       Изменение заданий теста --
 *       Соответственно получение тестов(а) ---
 * */

@Service
@RequiredArgsConstructor
public class TestService {
    private final TestDao testDao;
    private final QuestionDao questionDao;

    public void save(TransmittingTestDto requestDto) {
        TestEntity testEntity = toTestDto(requestDto, null);

        questionDao.saveAll(testEntity.getQuestions());
        testDao.save(testEntity);
    }

    public Boolean checkingOwnership(Long testId, Long userId) {
        Long issuerId = testDao.getIssuerId(testId);
        return issuerId == null || userId.equals(issuerId);
    }

    public void deleteById(Long testId) {
        testDao.deleteById(testId);
    }

    public Boolean update(Long id, TransmittingTestDto transmittingTestDto) {
        TestEntity testEntity = toTestDto(transmittingTestDto, id);
        try {
            testDao.update(testEntity);
            return true;
        } catch (DataAccessException exception) {
            testDao.save(testEntity);
            return false;
        }

    }

    public TestEntity findById(Long id) {
        return testDao.findById(id).orElseThrow(TestNotFoundException::new);
    }

    public List<TestEntity> findByUserId(Long userId) {
        List<TestEntity> userTestEntities = testDao.findByUserIdWithoutTask(userId);

        if (userTestEntities.isEmpty()) {
            throw new TestNotFoundException();
        }

        return userTestEntities;
    }



    public List<TestEntity> find(Long userId) {
        List<TestEntity> userTestEntities = testDao.findByUserIdWithoutTask(userId);

        if (userTestEntities.isEmpty()) {
            throw new TestNotFoundException();
        }

        return userTestEntities;
    }

    private TestEntity toTestDto(TransmittingTestDto requestDto, Long id) {
        return TestEntity.builder()
                .id(id)
                .visible(requestDto.getVisible())
                .description(requestDto.getDescription())
                .duration(requestDto.getDuration())
                .name(requestDto.getName())
                .numberTask(requestDto.getQuestions().size())
                .issuerId(requestDto.getIssuerId())
                .questions(requestDto.getQuestions().stream()
                        .map(question -> Question.builder()
                                .correctAnswer(question.correctAnswer())
                                .image(question.image())
                                .content(question.content())
                                .options(question.options())
                                .build())
                        .toList())
                .build();

    }

}
