package com.aichat.aichatdemo2.feature.chat.repository;

import com.aichat.aichatdemo2.feature.chat.dto.enumerated.MessageRole;
import com.aichat.aichatdemo2.feature.chat.dto.state.MessageInternalState;
import com.aichat.aichatdemo2.feature.chat.dto.state.MessagePageInternalState;
import com.aichat.aichatdemo2.feature.user.repository.UserRepository;
import com.aichat.aichatdemo2.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MessageRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private MessageRepository messageRepository;

    @Test
    void returnsLatestPageAndOlderCursor() {
        String login = loginOf(UUID.randomUUID().toString());
        UUID userId = userRepository.insert(login, login + "@example.com", "hash").getId();
        UUID chatId = chatRepository.insert(userId).getId();
        OffsetDateTime first = OffsetDateTime.parse("2024-01-01T00:00:00Z");
        messageRepository.insert(chatId, MessageRole.USER, "one", first);
        messageRepository.insert(chatId, MessageRole.USER, "two", first.plusMinutes(1));
        messageRepository.insert(chatId, MessageRole.ASSISTANT, "three", first.plusMinutes(2));

        assertThat(messageRepository.findLatest(chatId, 2)).extracting(MessageInternalState::getContent)
                .containsExactly("two", "three");

        MessagePageInternalState page = messageRepository.findPage(chatId, 2, null);
        assertThat(page.getItems()).extracting(MessageInternalState::getContent).containsExactly("two", "three");
        MessagePageInternalState older = messageRepository.findPage(chatId, 2, KeysetCursor.decode(page.getNextCursor()));
        assertThat(older.getItems()).extracting(MessageInternalState::getContent).containsExactly("one");
        assertThat(older.getNextCursor()).isNull();
    }
}
