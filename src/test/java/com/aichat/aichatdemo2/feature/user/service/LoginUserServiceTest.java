package com.aichat.aichatdemo2.feature.user.service;

import com.aichat.aichatdemo2.feature.user.dto.web.request.LoginRequest;
import com.aichat.aichatdemo2.feature.user.mapper.UserAutoMapperImpl;
import com.aichat.aichatdemo2.platform.exception.BusinessException;
import com.aichat.aichatdemo2.platform.exception.ErrorCodes;
import com.aichat.aichatdemo2.platform.security.AppUserDetails;
import com.aichat.aichatdemo2.platform.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUserServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    private LoginUserService loginUserService;

    @BeforeEach
    void setUp() {
        loginUserService = new LoginUserService(authenticationManager, jwtService, new UserAutoMapperImpl());
    }

    @Test
    void returnsTokenForValidCredentials() {
        UUID id = UUID.randomUUID();
        AppUserDetails details = new AppUserDetails(id, "alice", "hash", "a@b.c", null);
        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities()));
        when(jwtService.issue(id, "alice")).thenReturn("token");

        var auth = loginUserService.login(LoginRequest.builder().login("Alice").password("password123").build());

        assertThat(auth.getAccessToken()).isEqualTo("token");
        assertThat(auth.getUser().getEmail()).isEqualTo("a@b.c");
    }

    @Test
    void rejectsBadPassword() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));
        assertThatThrownBy(() -> loginUserService.login(LoginRequest.builder().login("alice").password("password123").build()))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getCode())
                .isEqualTo(ErrorCodes.UNAUTHORIZED);
    }
}
