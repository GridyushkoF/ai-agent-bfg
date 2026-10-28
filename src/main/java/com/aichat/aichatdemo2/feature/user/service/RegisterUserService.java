package com.aichat.aichatdemo2.feature.user.service;

import com.aichat.aichatdemo2.feature.user.dto.state.AuthInternalState;
import com.aichat.aichatdemo2.feature.user.dto.state.UserInternalState;
import com.aichat.aichatdemo2.feature.user.dto.web.request.RegisterRequest;
import com.aichat.aichatdemo2.feature.user.dto.web.response.AuthResponse;
import com.aichat.aichatdemo2.feature.user.mapper.UserAutoMapper;
import com.aichat.aichatdemo2.feature.user.repository.UserRepository;
import com.aichat.aichatdemo2.platform.exception.BusinessException;
import com.aichat.aichatdemo2.platform.exception.ErrorCodes;
import com.aichat.aichatdemo2.platform.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserAutoMapper userAutoMapper;

    /**
     * Регистрирует пользователя и выдаёт токен доступа.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String login = request.getLogin().trim().toLowerCase(Locale.ROOT);
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        log.debug("регистрация: вход login={}", login);
        if (userRepository.existsByLogin(login)) {
            throw BusinessException.createConflict(ErrorCodes.LOGIN_TAKEN, "Login is already taken");
        }
        if (userRepository.existsByEmail(email)) {
            throw BusinessException.createConflict(ErrorCodes.EMAIL_TAKEN, "Email is already taken");
        }
        try {
            UserInternalState user = userRepository.insert(login, email, passwordEncoder.encode(request.getPassword()));
            log.debug("регистрация: выход userId={}", user.getId());
            return userAutoMapper.mapToAuth(buildAuthState(user));
        } catch (DuplicateKeyException exception) {
            throw mapDuplicateKey(exception);
        }
    }

    private AuthInternalState buildAuthState(UserInternalState user) {
        return AuthInternalState.builder()
                .accessToken(jwtService.issue(user.getId(), user.getLogin()))
                .user(user)
                .build();
    }

    private static BusinessException mapDuplicateKey(DuplicateKeyException exception) {
        if ("ux_app_user_email".equals(findConstraint(exception))) {
            return BusinessException.createConflict(ErrorCodes.EMAIL_TAKEN, "Email is already taken");
        }
        return BusinessException.createConflict(ErrorCodes.LOGIN_TAKEN, "Login is already taken");
    }

    private static String findConstraint(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof PSQLException psql && psql.getServerErrorMessage() != null) {
                return psql.getServerErrorMessage().getConstraint();
            }
            current = current.getCause();
        }
        return null;
    }
}
