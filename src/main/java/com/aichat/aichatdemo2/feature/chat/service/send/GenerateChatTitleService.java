package com.aichat.aichatdemo2.feature.chat.service.send;

import com.aichat.aichatdemo2.feature.chat.ChatLimits;
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

    public Optional<String> generateAndStore(UUID chatId, String userContent) {
        log.debug("generate title enter chatId={}", chatId);
        try {
            String title = normalize(chatClient.prompt().system(SYSTEM).user(userContent).call().content());
            if (!chatRepository.setTitleIfAbsent(chatId, title)) {
                log.debug("generate title exit skipped chatId={}", chatId);
                return Optional.empty();
            }
            log.debug("generate title exit length={}", title.length());
            return Optional.of(title);
        } catch (RuntimeException exception) {
            log.error("generate title failed chatId={}", chatId, exception);
            return Optional.empty();
        }
    }

    static String normalize(String raw) {
        if (raw == null) {
            return "New chat";
        }
        String collapsed = raw.replaceAll("\\s+", " ").trim();
        if (collapsed.length() >= 2 && wrapped(collapsed)) {
            collapsed = collapsed.substring(1, collapsed.length() - 1).trim();
        }
        if (collapsed.isEmpty()) {
            return "New chat";
        }
        if (collapsed.length() <= ChatLimits.TITLE_MAX) {
            return collapsed;
        }
        return collapsed.substring(0, ChatLimits.TITLE_MAX).trim();
    }

    private static boolean wrapped(String value) {
        return (value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"));
    }
}
