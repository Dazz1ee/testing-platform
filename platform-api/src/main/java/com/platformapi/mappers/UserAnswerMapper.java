package com.platformapi.mappers;

import com.platformapi.models.UserAnswer;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class UserAnswerMapper implements RowMapper<UserAnswer> {

    @Override
    public UserAnswer mapRow(ResultSet rs, int rowNum) throws SQLException {
        return UserAnswer.builder()
                .answer(Arrays.stream(
                        (String []) (rs.getArray("answer").getArray()))
                        .toList()
                )
                .userTestId(rs.getLong("user_test_id"))
                .taskId(rs.getLong("task_id"))
                .correct(rs.getBoolean("is_correct"))
                .build();
    }
}
