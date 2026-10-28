package com.aichat.aichatdemo2.feature.user.service;

import com.aichat.aichatdemo2.feature.user.dto.state.UserInternalState;
import com.aichat.aichatdemo2.feature.user.dto.web.request.RegisterRequest;
import com.aichat.aichatdemo2.feature.user.mapper.UserAutoMapperImpl;
import com.aichat.aichatdemo2.feature.user.repository.UserRepository;
import com.aichat.aichatdemo2.platform.exception.BusinessException;
import com.aichat.aichatdemo2.platform.exception.ErrorCodes;
import com.aichat.aichatdemo2.platform.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    private RegisterUserService registerUserService;

    @BeforeEach
    void setUp() {
        registerUserService = new RegisterUserService(
                userRepository, passwordEncoder, jwtService, new UserAutoMapperImpl());
    }

    @Test
    void storesHashedPasswordAndToken() {
        UUID id = UUID.randomUUID();
        when(passwordEncoder.encode("password123")).thenReturn("hash");
        when(userRepository.insert(eq("alice"), eq("a@b.c"), eq("hash")))
                .thenReturn(UserInternalState.builder().id(id).login("alice").email("a@b.c").build());
        when(jwtService.issue(id, "alice")).thenReturn("token");

        var auth = registerUserService.register(request());

        assertThat(auth.getAccessToken()).isEqualTo("token");
        assertThat(auth.getUser().getLogin()).isEqualTo("alice");
        verify(userRepository, never()).insert(eq("alice"), eq("a@b.c"), eq("password123"));
    }

    @Test
    void rejectsTakenLogin() {
        when(userRepository.existsByLogin("alice")).thenReturn(true);
        assertThatThrownBy(() -> registerUserService.register(request()))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getCode())
                .isEqualTo(ErrorCodes.LOGIN_TAKEN);
        verify(userRepository, never()).insert(anyString(), anyString(), anyString());
    }

    @Test
    void mapsEmailConstraintWithoutParsingMessageText() {
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(userRepository.insert(anyString(), anyString(), anyString()))
                .thenThrow(duplicate("ux_app_user_email", "duplicate key"));
        assertThatThrownBy(() -> registerUserService.register(request()))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getCode())
                .isEqualTo(ErrorCodes.EMAIL_TAKEN);
    }

    private static RegisterRequest request() {
        return RegisterRequest.builder().login("Alice").email("A@b.c").password("password123").build();
    }

    private static DuplicateKeyException duplicate(String constraint, String message) {
        var server = new ServerErrorMessage("SERROR\0C23505\0n" + constraint + "\0M" + message + "\0\0");
        return new DuplicateKeyException(message, new PSQLException(server));
    }
}
