package com.platformapi.services;

import com.platformapi.dao.QuestionDao;
import com.platformapi.dao.TestDao;
import com.platformapi.exceptions.TestNotFoundException;
import com.platformapi.models.Question;
import com.platformapi.models.TestEntity;
import com.platformapi.models.TransmittingQuestionDto;
import com.platformapi.models.TransmittingTestDto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class TestServiceTest {
    TestDao testDao;

    QuestionDao questionDao;

    TestService testService;

    @BeforeEach
    void init(@Mock TestDao testDao, @Mock QuestionDao questionDao) {
        this.testDao = testDao;
        this.questionDao = questionDao;
        testService = new TestService(testDao, questionDao);
    }


    private TestEntity getTestDto(TransmittingTestDto transmittingTestDto) {
        return TestEntity.builder()
                .issuerId(transmittingTestDto.getIssuerId())
                .name(transmittingTestDto.getName())
                .numberTask(1)
                .duration(transmittingTestDto.getDuration())
                .description(transmittingTestDto.getDescription())
                .visible(transmittingTestDto.getVisible())
                .questions(transmittingTestDto.getQuestions().stream()
                        .map(question -> Question.builder().content(question.content())
                                .image(question.image())
                                .correctAnswer(question.correctAnswer())
                                .options(question.options())
                                .build()).toList())
                .build();
    }
    private TestEntity getTestDto(Long id) {
        TestEntity testEntity = TestEntity.builder()
                .id(id)
                .issuerId(1L)
                .name("test")
                .numberTask(1)
                .description("test")
                .visible(true)
                .questions(List.of(Question.builder().content("test")
                                .image(null)
                                .correctAnswer(List.of("test"))
                                .options(List.of("test"))
                                .build()))
                .build();
        return testEntity;
    }


    @Test
    void save() {
        TransmittingTestDto transmittingTestDto = new TransmittingTestDto(1L, "test", "test", 133, true,
                List.of(new TransmittingQuestionDto(
                        null, null, "test", List.of("test"), List.of("test"))
                ));

        testService.save(transmittingTestDto);
        TestEntity testEntity = getTestDto(transmittingTestDto);
        verify(testDao, times(1)).save(testEntity);
    }

    @Test
    void checkingOwnershipWhenTrue() {
        when(testDao.getIssuerId(5L)).thenReturn(3L);

        Boolean actual = testService.checkingOwnership(5L, 3L);

        assertThat(actual).isTrue();
    }

    @Test
    void deleteById() {
        testService.deleteById(3L);
        verify(testDao, times(1)).deleteById(3L);
    }

    @Test
    void update() {
        TransmittingTestDto transmittingTestDto = new TransmittingTestDto(1L, "test", "test", 133, true,
                List.of(new TransmittingQuestionDto(
                        null, null, "test", List.of("test"), List.of("test"))
                ));

        TestEntity testEntity = getTestDto(transmittingTestDto);
        testEntity.setId(1L);
        assertThat(testService.update(1L, transmittingTestDto)).isTrue();
        verify(testDao).update(testEntity);
        verify(testDao, times(0)).save(any());
    }

    @Test
    void updateWhenNotExists() {
        TransmittingTestDto transmittingTestDto = new TransmittingTestDto(1L, "test", "test", 133, true,
                List.of(new TransmittingQuestionDto(
                        null, null, "test", List.of("test"), List.of("test"))
                ));

        TestEntity testEntity = getTestDto(transmittingTestDto);
        when(testDao.update(testEntity)).thenThrow(DataIntegrityViolationException.class);
        assertThat(testService.update(null, transmittingTestDto)).isFalse();
        verify(testDao, times(1)).save(testEntity);

    }

    @Test
    void findById() {
        Long id = 1L;
        TestEntity testEntity = getTestDto(1L);
        when(testDao.findById(id)).thenReturn(Optional.of(testEntity));

        assertThat(testService.findById(id)).isEqualTo(testEntity);
    }

    @Test
    void findByIdThrowException() {
        Long id = 1L;
        when(testDao.findById(id)).thenReturn(Optional.empty());

        assertThrows(TestNotFoundException.class, () -> testService.findById(id));
    }


    @Test
    void findByUserId() {
        Long id = 1L;
        TestEntity testEntity = getTestDto(1L);
        when(testDao.findByUserIdWithoutTask(id)).thenReturn(List.of(testEntity));

        List<TestEntity> actual = testDao.findByUserIdWithoutTask(id);
        assertThat(actual).hasSize(1).contains(testEntity);
    }

    @Test
    void findByUserIdThrowException() {
        Long id = 1L;
        when(testDao.findByUserIdWithoutTask(id)).thenReturn(List.of());

        List<TestEntity> actual = testDao.findByUserIdWithoutTask(id);
        assertThrows(TestNotFoundException.class, () -> testService.findByUserId(id));
    }
}