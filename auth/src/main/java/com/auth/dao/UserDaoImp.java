package com.auth.dao;

import com.auth.exceptions.DuplicateEmailException;
import com.auth.models.CustomUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserDaoImp implements UserDao{
    private final JdbcTemplate jdbcTemplate;
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Optional<CustomUser> findByEmail(String email) {
        String sql = "SELECT id, first_name, second_name, email, password FROM users WHERE email = ?";
        CustomUser user = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<CustomUser>(CustomUser.class), email);
        return Optional.ofNullable(user);
    }

    @Override
    public Optional<CustomUser> save(CustomUser user) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("first_name", user.getFirstName());
        parameters.put("second_name", user.getSecondName());
        parameters.put("email", user.getEmail());
        parameters.put("password", user.getPassword());

        try {
            long id = jdbcInsert.executeAndReturnKey(parameters).longValue();
            user.setId(id);
            return Optional.of(user);
        } catch (DuplicateKeyException ex) {
            throw new DuplicateEmailException();
        }
    }

}
