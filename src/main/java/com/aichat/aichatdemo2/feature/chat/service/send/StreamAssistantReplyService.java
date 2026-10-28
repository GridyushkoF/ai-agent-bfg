package com.aichat.aichatdemo2.feature.chat.service.send;

import com.aichat.aichatdemo2.feature.chat.dto.state.MessageInternalState;
import com.aichat.aichatdemo2.feature.chat.dto.state.MessageRole;
import com.aichat.aichatdemo2.platform.exception.TechnicalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreamAssistantReplyService {

    static final String SYSTEM = "You are a helpful assistant. Answer in Markdown.";

    private final ChatClient chatClient;

    /**
     * Запрашивает у модели ответ по истории и отдаёт куски текста по мере генерации.
     */
    public String stream(List<MessageInternalState> history, Consumer<String> onDelta) {
        log.debug("поток ответа: вход messages={}", history.size());
        StringBuilder full = new StringBuilder();
        chatClient.prompt()
                .messages(toMessages(history))
                .stream()
                .content()
                .doOnNext(chunk -> append(chunk, full, onDelta))
                .blockLast(Duration.ofMinutes(4));
        if (full.isEmpty()) {
            throw new TechnicalException("AI returned an empty response", null);
        }
        log.debug("поток ответа: выход chars={}", full.length());
        return full.toString();
    }

    private static List<Message> toMessages(List<MessageInternalState> history) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(SYSTEM));
        for (MessageInternalState item : history) {
            messages.add(item.getRole() == MessageRole.USER
                    ? new UserMessage(item.getContent())
                    : new AssistantMessage(item.getContent()));
        }
        return messages;
    }

    private static void append(String chunk, StringBuilder full, Consumer<String> onDelta) {
        if (chunk == null || chunk.isEmpty()) {
            return;
        }
        full.append(chunk);
        onDelta.accept(chunk);
    }
}
