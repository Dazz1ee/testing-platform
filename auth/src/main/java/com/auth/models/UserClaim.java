package com.auth.models;

import org.springframework.security.core.GrantedAuthority;

public record UserClaim(Long id, String email, java.util.Collection<? extends GrantedAuthority> authorities) {
}
