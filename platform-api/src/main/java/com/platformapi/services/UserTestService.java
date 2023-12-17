package com.platformapi.services;

import com.platformapi.dao.TestDao;
import com.platformapi.dao.UserAnswerDao;
import com.platformapi.dao.UserTestDao;
import com.platformapi.exceptions.TestAlreadyStarted;
import com.platformapi.exceptions.TestNotStartedException;
import com.platformapi.exceptions.WrongUserTestData;
import com.platformapi.models.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class UserTestService {
    private final UserTestDao userTestDao;
    private final TestDao testDao;
    private final UserAnswerDao userAnswerDao;

    public TransmittingTestDto startTest(TestStartingDto testStartingDto, Long userId) {
        TestEntity testEntity = testDao
                .findById(testStartingDto.getTestId())
                .orElseThrow(WrongUserTestData::new);

        UserTest userTest = UserTest.builder()
                .testId(testStartingDto.getTestId())
                .userId(userId)
                .startTime(Instant.now().truncatedTo(ChronoUnit.MINUTES))
                .build();
        try {
            userTestDao.save(userTest);
        } catch (DuplicateKeyException duplicateKeyException) {
            throw new TestAlreadyStarted(duplicateKeyException);
        } catch (DataAccessException dataAccessException) {
            throw new WrongUserTestData(dataAccessException);
        }

        return new TransmittingTestDto(testEntity);
    }

    public UserTest getResult(Long testId, Long userId) {
        return userTestDao.find(userId, testId).orElseThrow(TestNotStartedException::new);
    }

    public Boolean isVisible(Long testId) {
        return testDao.isVisible(testId).orElse(false);
    }
}
