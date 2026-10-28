package com.aichat.aichatdemo2.feature.chat.service.send;

import com.aichat.aichatdemo2.feature.chat.ChatProperties;
import com.aichat.aichatdemo2.feature.chat.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateChatTitleService {

    static final String SYSTEM = """
            Write a short chat title for the user message.
            Return only the title, at most 8 words, in the same language as the user.
            Do not wrap the title in quotes.
            """;

    private final ChatClient chatClient;
    private final ChatRepository chatRepository;
    private final ChatProperties chatProperties;

    /**
     * Один раз задаёт заголовок чата по первому сообщению. Ошибка модели не прерывает ответ.
     */
    public Optional<String> generateAndStore(UUID chatId, String userContent) {
        log.debug("заголовок чата: вход chatId={}", chatId);
        try {
            String title = normalize(chatClient.prompt().system(SYSTEM).user(userContent).call().content());
            if (!chatRepository.setTitleIfAbsent(chatId, title)) {
                log.debug("заголовок чата: пропуск chatId={}", chatId);
                return Optional.empty();
            }
            log.debug("заголовок чата: выход length={}", title.length());
            return Optional.of(title);
        } catch (RuntimeException exception) {
            log.error("заголовок чата не создан chatId={}", chatId, exception);
            return Optional.empty();
        }
    }

    String normalize(String raw) {
        if (raw == null) {
            return "New chat";
        }
        String collapsed = raw.replaceAll("\\s+", " ").trim();
        if (collapsed.length() >= 2 && isWrapped(collapsed)) {
            collapsed = collapsed.substring(1, collapsed.length() - 1).trim();
        }
        if (collapsed.isEmpty()) {
            return "New chat";
        }
        int titleMax = Math.min(chatProperties.getTitleMax(), ChatProperties.TITLE_COLUMN_LIMIT);
        if (collapsed.length() <= titleMax) {
            return collapsed;
        }
        return collapsed.substring(0, titleMax).trim();
    }

    private static boolean isWrapped(String value) {
        return (value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"));
    }
}
