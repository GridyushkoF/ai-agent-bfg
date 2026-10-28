package com.aichat.aichatdemo2.feature.chat.repository;

import com.aichat.aichatdemo2.feature.chat.dto.state.ChatInternalState;
import com.aichat.aichatdemo2.feature.chat.dto.state.ChatPageInternalState;
import com.aichat.aichatdemo2.feature.user.repository.UserRepository;
import com.aichat.aichatdemo2.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ChatRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ChatRepository chatRepository;

    @Test
    void pagesChatsAndSearchesTitleOnDatabase() {
        String login = loginOf(UUID.randomUUID().toString());
        UUID userId = userRepository.insert(login, login + "@example.com", "hash").getId();
        ChatInternalState older = chatRepository.insert(userId);
        ChatInternalState newer = chatRepository.insert(userId);
        chatRepository.touch(older.getId(), OffsetDateTime.parse("2020-01-01T00:00:00Z"));
        chatRepository.touch(newer.getId(), OffsetDateTime.parse("2024-01-01T00:00:00Z"));
        assertThat(chatRepository.setTitleIfAbsent(older.getId(), "100% done")).isTrue();
        assertThat(chatRepository.setTitleIfAbsent(older.getId(), "again")).isFalse();
        assertThat(chatRepository.setTitleIfAbsent(newer.getId(), "Alpha Reactors")).isTrue();

        ChatPageInternalState first = chatRepository.search(userId, null, 1, null);
        assertThat(first.getItems()).extracting(ChatInternalState::getId).containsExactly(newer.getId());
        ChatPageInternalState second = chatRepository.search(userId, null, 1, KeysetCursor.decode(first.getNextCursor()));
        assertThat(second.getItems()).extracting(ChatInternalState::getId).containsExactly(older.getId());
        assertThat(second.getNextCursor()).isNull();

        assertThat(chatRepository.search(userId, "aLpHa", 20, null).getItems())
                .extracting(ChatInternalState::getId).containsExactly(newer.getId());
        assertThat(chatRepository.search(userId, "%", 20, null).getItems())
                .extracting(ChatInternalState::getId).containsExactly(older.getId());
        assertThat(chatRepository.findOwned(newer.getId(), UUID.randomUUID())).isEmpty();
    }
}
