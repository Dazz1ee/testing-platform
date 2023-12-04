package com.platformapi.configurations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Slf4j
@Component
public class CustomJdbcTemplate extends JdbcTemplate {

    public CustomJdbcTemplate(DataSource dataSource) {
        super(dataSource);
    }


    @Override
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException {
        try {
            return super.queryForObject(sql, rowMapper, args);
        } catch (EmptyResultDataAccessException ex) {
            log.debug("Object not found", ex);
            return null;
        }
    }

    @Override
    public <T> T queryForObject(String sql, Class<T> requiredType, Object... args) throws DataAccessException {
        try {
            return super.queryForObject(sql, requiredType, args);
        } catch (EmptyResultDataAccessException ex) {
            log.debug("Object not found", ex);
            return null;
        }
    }

    @Override
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper) throws DataAccessException {
        try {
            return super.queryForObject(sql, rowMapper);
        } catch (EmptyResultDataAccessException ex) {
            log.debug("Object not found", ex);
            return null;
        }
    }
}
