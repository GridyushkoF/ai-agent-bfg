package com.aichat.aichatdemo2.platform.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@ToString(exclude = {"password", "email"})
@RequiredArgsConstructor
public class AppUserDetails implements UserDetails {

    private final UUID userId;
    private final String username;
    private final String password;
    private final String email;
    private final OffsetDateTime createdAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }
}
