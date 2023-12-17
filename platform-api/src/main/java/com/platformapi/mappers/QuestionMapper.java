package com.platformapi.mappers;

import com.platformapi.models.Question;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;

public class QuestionMapper implements RowMapper<Question> {

    @Override
    public Question mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Question.builder()
                .id(rs.getLong("id"))
                .content(rs.getString("content"))
                .image(rs.getObject("image", UUID.class))
                .options(Arrays.stream(
                                (String[]) (rs.getArray("options").getArray()))
                        .toList()
                )
                .correctAnswer(Arrays.stream(
                                (String[]) (rs.getArray("correct_answer").getArray()))
                        .toList()
                )
                .build();
    }
}
