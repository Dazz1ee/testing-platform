package com.auth.models;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@Setter
public class CustomPrincipal implements UserDetails {
    private Long id;
    private String email;
    private String firstName;
    private String secondName;
    private String password;


    // TODO AUTHORITIES IN OAUTH
    public CustomPrincipal(CustomUser user) {
        id = user.getId();
        email = user.getEmail();
        firstName = user.getFirstName();
        secondName = user.getSecondName();
        password = user.getPassword();
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new ArrayList<>();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
