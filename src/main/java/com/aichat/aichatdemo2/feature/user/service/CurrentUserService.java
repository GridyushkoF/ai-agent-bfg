package com.aichat.aichatdemo2.feature.user.service;

import com.aichat.aichatdemo2.feature.user.repository.UserRepository;
import com.aichat.aichatdemo2.feature.user.state.UserInternalState;
import com.aichat.aichatdemo2.platform.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserInternalState me(UUID userId) {
        log.debug("me enter userId={}", userId);
        UserInternalState user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.unauthorized("Authentication required"));
        log.debug("me exit login={}", user.getLogin());
        return user;
    }
}
