package com.aichat.aichatdemo2.feature.chat.service.send;

import com.aichat.aichatdemo2.feature.chat.repository.ChatRepository;
import com.aichat.aichatdemo2.support.StubChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GenerateChatTitleServiceTest {

    private final ChatRepository chatRepository = mock(ChatRepository.class);
    private final GenerateChatTitleService service =
            new GenerateChatTitleService(ChatClient.create(new StubChatModel()), chatRepository);

    @Test
    void storesNormalizedTitleOnce() {
        UUID chatId = UUID.randomUUID();
        when(chatRepository.setTitleIfAbsent(chatId, "Hello world")).thenReturn(true);

        assertThat(service.generateAndStore(chatId, "  \"Hello   world\"  ")).contains("Hello world");
        verify(chatRepository).setTitleIfAbsent(chatId, "Hello world");
    }

    @Test
    void skipsWhenTitleAlreadyStored() {
        UUID chatId = UUID.randomUUID();
        when(chatRepository.setTitleIfAbsent(chatId, "Hello")).thenReturn(false);
        assertThat(service.generateAndStore(chatId, "Hello")).isEmpty();
    }

    @Test
    void normalizeFallsBackWhenBlank() {
        assertThat(GenerateChatTitleService.normalize("   ")).isEqualTo("New chat");
        assertThat(GenerateChatTitleService.normalize(null)).isEqualTo("New chat");
    }
}
