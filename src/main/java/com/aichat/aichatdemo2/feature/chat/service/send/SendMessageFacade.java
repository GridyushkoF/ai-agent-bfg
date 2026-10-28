package com.aichat.aichatdemo2.feature.chat.service.send;

import com.aichat.aichatdemo2.feature.chat.dto.state.PreparedSendInternalState;
import com.aichat.aichatdemo2.feature.chat.dto.web.request.SendMessageRequest;
import com.aichat.aichatdemo2.feature.chat.service.send.ChatSseWriter.ClientDisconnectedException;
import com.aichat.aichatdemo2.platform.exception.TechnicalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendMessageFacade {

    private final SaveUserMessageService saveUserMessageService;
    private final StreamAssistantReplyService streamAssistantReplyService;
    private final GenerateChatTitleService generateChatTitleService;
    private final SaveAssistantMessageService saveAssistantMessageService;
    private final ChatSseWriter chatSseWriter;
    private final TaskExecutor chatStreamExecutor;

    /**
     * Сохраняет сообщение пользователя и стримит ответ бота в SSE.
     */
    public SseEmitter send(UUID userId, UUID chatId, SendMessageRequest request) {
        log.debug("отправка сообщения: вход userId={} chatId={}", userId, chatId);
        saveUserMessageService.requireOwned(userId, chatId);
        SseEmitter emitter = chatSseWriter.open();
        try {
            chatStreamExecutor.execute(() -> produce(userId, chatId, request.getContent(), emitter));
        } catch (RejectedExecutionException exception) {
            log.error("очередь ответов переполнена chatId={}", chatId, exception);
            throw new TechnicalException("AI service is unavailable", exception);
        }
        log.debug("отправка сообщения: выход chatId={}", chatId);
        return emitter;
    }

    private void produce(UUID userId, UUID chatId, String content, SseEmitter emitter) {
        PreparedSendInternalState prepared = null;
        boolean assistantSaved = false;
        try {
            prepared = saveUserMessageService.prepare(userId, chatId, content);
            String answer = streamAssistantReplyService.stream(
                    prepared.getHistory(), chunk -> chatSseWriter.send(emitter, ChatSseWriter.DELTA, Map.of("text", chunk)));
            UUID assistantId = saveAssistantMessageService.save(prepared.getChatId(), answer);
            assistantSaved = true;
            publishAfterReply(prepared, emitter, assistantId);
            emitter.complete();
        } catch (RuntimeException exception) {
            finishFailed(chatId, emitter, prepared, assistantSaved, exception);
        }
    }

    private void publishAfterReply(PreparedSendInternalState prepared, SseEmitter emitter, UUID assistantId) {
        if (prepared.isTitleMissing()) {
            generateChatTitleService.generateAndStore(prepared.getChatId(), prepared.getUserContent())
                    .ifPresent(title -> chatSseWriter.send(emitter, ChatSseWriter.TITLE, Map.of("title", title)));
        }
        chatSseWriter.send(emitter, ChatSseWriter.DONE, Map.of(
                "assistantMessageId", assistantId, "chatId", prepared.getChatId()));
    }

    private void finishFailed(UUID chatId, SseEmitter emitter, PreparedSendInternalState prepared,
                              boolean assistantSaved, RuntimeException exception) {
        discardUnanswered(prepared, assistantSaved);
        if (exception instanceof ClientDisconnectedException) {
            log.debug("клиент отключился от SSE chatId={}", chatId);
            emitter.complete();
            return;
        }
        log.error("отправка сообщения не удалась chatId={}", chatId, exception);
        chatSseWriter.completeWithError(emitter);
    }

    private void discardUnanswered(PreparedSendInternalState prepared, boolean assistantSaved) {
        if (prepared == null || assistantSaved) {
            return;
        }
        try {
            saveUserMessageService.discard(prepared.getUserMessageId());
        } catch (RuntimeException exception) {
            log.error("не удалось удалить сообщение пользователя chatId={}", prepared.getChatId(), exception);
        }
    }
}
