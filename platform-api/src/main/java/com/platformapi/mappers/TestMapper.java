package com.platformapi.mappers;

import com.platformapi.models.Question;
import com.platformapi.models.TestEntity;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class TestMapper implements ResultSetExtractor<TestEntity> {


    @Override
    public TestEntity extractData(ResultSet rs) throws SQLException, DataAccessException {
        TestEntity testEntity = null;

        while (rs.next()) {
            if (testEntity == null) {
                testEntity = TestEntity.builder()
                        .id(rs.getLong("id"))
                        .issuerId(rs.getLong("issuer_id"))
                        .name(rs.getString("name"))
                        .description(rs.getString("description"))
                        .duration(rs.getObject("duration", Integer.class))
                        .visible(rs.getBoolean("visible"))
                        .numberTask(rs.getInt("number_task"))
                        .questions(new ArrayList<>())
                        .build();
            }
            Array options = rs.getArray("options");
            Array correctAnswer = rs.getArray("correct_answer");

            testEntity.getQuestions().add(
                    Question.builder()
                            .id(rs.getLong("question_id"))
                            .content(rs.getString("content"))
                            .image(rs.getObject("image", UUID.class))
                            .options(options == null ? null : Arrays.stream(
                                            (String[]) options.getArray())
                                    .toList()
                            )
                            .correctAnswer(correctAnswer == null ? null : Arrays.stream(
                                            (String[]) correctAnswer.getArray())
                                    .toList()
                            )
                            .build()
            );

        }

        return testEntity;
    }
}
