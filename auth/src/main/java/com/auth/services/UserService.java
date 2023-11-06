package com.auth.services;

import com.auth.dao.UserDao;
import com.auth.models.CustomUser;
import com.auth.models.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.CharBuffer;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserDao userDao;

    private final PasswordEncoder passwordEncoder;

    public void save(UserDto userDto) {
        CustomUser customUser = CustomUser.builder()
                .email(userDto.email())
                .password(passwordEncoder.encode(CharBuffer.wrap(userDto.password())))
                .firstName(userDto.firstName())
                .secondName(userDto.secondName())
                .build();

        Arrays.fill(userDto.password(), '\0');
        log.debug(customUser.getFirstName());
        userDao.save(customUser);
    }

}
