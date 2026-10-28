package com.aichat.aichatdemo2.feature.user.service;

import com.aichat.aichatdemo2.feature.user.dto.RegisterRequest;
import com.aichat.aichatdemo2.feature.user.repository.UserRepository;
import com.aichat.aichatdemo2.feature.user.state.AuthInternalState;
import com.aichat.aichatdemo2.feature.user.state.UserInternalState;
import com.aichat.aichatdemo2.platform.exception.BusinessException;
import com.aichat.aichatdemo2.platform.exception.ErrorCodes;
import com.aichat.aichatdemo2.platform.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Transactional
    public AuthInternalState register(RegisterRequest request) {
        String login = request.getLogin().trim().toLowerCase(Locale.ROOT);
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        log.debug("register enter login={}", login);
        if (userRepository.existsByLogin(login)) {
            throw BusinessException.conflict(ErrorCodes.LOGIN_TAKEN, "Login is already taken");
        }
        if (userRepository.existsByEmail(email)) {
            throw BusinessException.conflict(ErrorCodes.EMAIL_TAKEN, "Email is already taken");
        }
        try {
            UserInternalState user = userRepository.insert(login, email, passwordEncoder.encode(request.getPassword()));
            AuthInternalState auth = tokenFor(user);
            log.debug("register exit userId={}", user.getId());
            return auth;
        } catch (DuplicateKeyException exception) {
            throw duplicate(exception);
        }
    }

    private AuthInternalState tokenFor(UserInternalState user) {
        return AuthInternalState.builder()
                .accessToken(jwtService.issue(user.getId(), user.getLogin()))
                .user(user)
                .build();
    }

    private static BusinessException duplicate(DuplicateKeyException exception) {
        String message = exception.getMostSpecificCause().getMessage();
        if (message != null && message.contains("ux_app_user_email")) {
            return BusinessException.conflict(ErrorCodes.EMAIL_TAKEN, "Email is already taken");
        }
        return BusinessException.conflict(ErrorCodes.LOGIN_TAKEN, "Login is already taken");
    }
}
