package com.aichat.aichatdemo2.feature.user.service;

import com.aichat.aichatdemo2.feature.user.repository.UserRepository;
import com.aichat.aichatdemo2.feature.user.state.UserInternalState;
import com.aichat.aichatdemo2.platform.security.AppUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        log.debug("load user enter login={}", username);
        UserInternalState user = userRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        log.debug("load user exit userId={}", user.getId());
        return new AppUserDetails(user.getId(), user.getLogin(), user.getPasswordHash(), user.getEmail(), user.getCreatedAt());
    }
}
