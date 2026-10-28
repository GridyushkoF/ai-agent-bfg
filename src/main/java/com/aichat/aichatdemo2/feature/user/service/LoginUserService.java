package com.aichat.aichatdemo2.feature.user.service;

import com.aichat.aichatdemo2.feature.user.dto.LoginRequest;
import com.aichat.aichatdemo2.feature.user.state.AuthInternalState;
import com.aichat.aichatdemo2.feature.user.state.UserInternalState;
import com.aichat.aichatdemo2.platform.exception.BusinessException;
import com.aichat.aichatdemo2.platform.security.AppUserDetails;
import com.aichat.aichatdemo2.platform.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginUserService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthInternalState login(LoginRequest request) {
        String login = request.getLogin().trim().toLowerCase(Locale.ROOT);
        log.debug("login enter login={}", login);
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(login, request.getPassword()));
            AppUserDetails user = (AppUserDetails) authentication.getPrincipal();
            AuthInternalState auth = AuthInternalState.builder()
                    .accessToken(jwtService.issue(user.getUserId(), user.getUsername()))
                    .user(UserInternalState.builder()
                            .id(user.getUserId())
                            .login(user.getUsername())
                            .email(user.getEmail())
                            .createdAt(user.getCreatedAt())
                            .build())
                    .build();
            log.debug("login exit userId={}", user.getUserId());
            return auth;
        } catch (BadCredentialsException exception) {
            throw BusinessException.unauthorized("Invalid login or password");
        }
    }
}
