package com.aichat.aichatdemo2.feature.user.repository;

import com.aichat.aichatdemo2.feature.user.dto.state.UserInternalState;
import com.aichat.aichatdemo2.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void insertsAndFindsByLoginAndId() {
        String login = loginOf(UUID.randomUUID().toString());
        String email = login + "@example.com";
        assertThat(userRepository.existsByLogin(login)).isFalse();
        assertThat(userRepository.existsByEmail(email)).isFalse();

        UserInternalState user = userRepository.insert(login, email, "hash");

        assertThat(userRepository.existsByLogin(login)).isTrue();
        assertThat(userRepository.existsByEmail(email)).isTrue();
        assertThat(userRepository.findByLogin(login)).get().extracting(UserInternalState::getId).isEqualTo(user.getId());
        assertThat(userRepository.findById(user.getId())).get().extracting(UserInternalState::getPasswordHash).isEqualTo("hash");
        assertThat(userRepository.findByLogin("missing-" + login)).isEmpty();
    }
}
