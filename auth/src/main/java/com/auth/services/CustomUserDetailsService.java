package com.auth.services;

import com.auth.dao.UserDao;
import com.auth.models.CustomPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserDao userDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new CustomPrincipal(userDao.findByEmail(username).orElseThrow(() ->
                new UsernameNotFoundException(String.format("User [%s] not found", username))));
    }
}
