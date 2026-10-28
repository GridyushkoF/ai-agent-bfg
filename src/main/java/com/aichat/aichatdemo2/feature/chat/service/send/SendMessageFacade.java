package com.aichat.aichatdemo2.feature.chat.service.send;

import com.aichat.aichatdemo2.feature.chat.dto.state.PreparedSendInternalState;
import com.aichat.aichatdemo2.feature.chat.dto.web.request.SendMessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendMessageFacade {

    static final String DELTA = "delta";
    static final String TITLE = "title";
    static final String DONE = "done";
    static final String ERROR = "error";

    private final SaveUserMessageService saveUserMessageService;
    private final StreamAssistantReplyService streamAssistantReplyService;
    private final GenerateChatTitleService generateChatTitleService;
    private final SaveAssistantMessageService saveAssistantMessageService;
    private final ObjectMapper objectMapper;
    private final TaskExecutor chatStreamExecutor;

    /**
     * Сохраняет сообщение пользователя и стримит ответ бота в SSE.
     */
    public SseEmitter send(UUID userId, UUID chatId, SendMessageRequest request) {
        log.debug("отправка сообщения: вход userId={} chatId={}", userId, chatId);
        PreparedSendInternalState prepared = saveUserMessageService.prepare(userId, chatId, request.getContent());
        SseEmitter emitter = new SseEmitter(Duration.ofMinutes(5).toMillis());
        emitter.onTimeout(emitter::complete);
        chatStreamExecutor.execute(() -> produce(prepared, emitter));
        log.debug("отправка сообщения: выход chatId={}", chatId);
        return emitter;
    }

    private void produce(PreparedSendInternalState prepared, SseEmitter emitter) {
        try {
            String answer = streamAssistantReplyService.stream(prepared.getHistory(), chunk -> send(emitter, DELTA, Map.of("text", chunk)));
            UUID assistantId = saveAssistantMessageService.save(prepared.getChatId(), answer);
            if (prepared.isTitleMissing()) {
                generateChatTitleService.generateAndStore(prepared.getChatId(), prepared.getUserContent())
                        .ifPresent(title -> send(emitter, TITLE, Map.of("title", title)));
            }
            send(emitter, DONE, Map.of("assistantMessageId", assistantId, "chatId", prepared.getChatId()));
            emitter.complete();
        } catch (ClientDisconnectedException exception) {
            log.debug("клиент отключился от SSE chatId={}", prepared.getChatId());
            emitter.complete();
        } catch (RuntimeException exception) {
            log.error("отправка сообщения не удалась chatId={}", prepared.getChatId(), exception);
            completeWithError(emitter);
        }
    }

    private void completeWithError(SseEmitter emitter) {
        try {
            send(emitter, ERROR, Map.of("message", "AI service is unavailable"));
            emitter.complete();
        } catch (ClientDisconnectedException exception) {
            emitter.complete();
        }
    }

    private void send(SseEmitter emitter, String event, Map<String, ?> payload) {
        try {
            emitter.send(SseEmitter.event().name(event).data(objectMapper.writeValueAsString(payload)));
        } catch (IOException exception) {
            throw new ClientDisconnectedException();
        }
    }

    private static final class ClientDisconnectedException extends RuntimeException {
    }
}
