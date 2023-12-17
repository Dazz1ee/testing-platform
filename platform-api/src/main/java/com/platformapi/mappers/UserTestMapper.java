package com.platformapi.mappers;

import com.platformapi.models.UserTest;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;


public class UserTestMapper implements RowMapper<UserTest> {
    @Override
    public UserTest mapRow(ResultSet rs, int rowNum) throws SQLException {
        return UserTest.builder()
                .id(rs.getLong("id"))
                .testId(rs.getLong("test_id"))
                .userId(rs.getLong("user_id"))
                .countCorrect(rs.getObject("count_correct", Integer.class))
                .startTime(rs.getTimestamp("started_time").toInstant())
                .finishTime(rs.getTimestamp("completed_time") != null ?
                        rs.getTimestamp("completed_time").toInstant() :
                        null
                )
                .build();
    }
}
