package com.aichat.aichatdemo2.feature.user.service;

import com.aichat.aichatdemo2.feature.user.dto.state.UserInternalState;
import com.aichat.aichatdemo2.feature.user.dto.web.response.UserResponse;
import com.aichat.aichatdemo2.feature.user.mapper.UserAutoMapper;
import com.aichat.aichatdemo2.feature.user.repository.UserRepository;
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
    private final UserAutoMapper userAutoMapper;

    /**
     * Возвращает профиль пользователя.
     */
    @Transactional(readOnly = true)
    public UserResponse getProfile(UUID userId) {
        log.debug("профиль: вход userId={}", userId);
        UserInternalState user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.createUnauthorized("Authentication required"));
        log.debug("профиль: выход login={}", user.getLogin());
        return userAutoMapper.mapToUser(user);
    }
}
