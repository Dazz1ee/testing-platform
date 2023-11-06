package com.auth.dao;

import com.auth.models.CustomUser;

import java.util.Optional;

public interface UserDao {
    Optional<CustomUser> findByEmail(String email);
    Optional<CustomUser> save(CustomUser user);
}
