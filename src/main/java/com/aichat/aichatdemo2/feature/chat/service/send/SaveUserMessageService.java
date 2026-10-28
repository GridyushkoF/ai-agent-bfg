package com.aichat.aichatdemo2.feature.chat.service.send;

import com.aichat.aichatdemo2.feature.chat.ChatProperties;
import com.aichat.aichatdemo2.feature.chat.dto.enumerated.MessageRole;
import com.aichat.aichatdemo2.feature.chat.dto.state.ChatInternalState;
import com.aichat.aichatdemo2.feature.chat.dto.state.PreparedSendInternalState;
import com.aichat.aichatdemo2.feature.chat.repository.ChatRepository;
import com.aichat.aichatdemo2.feature.chat.repository.MessageRepository;
import com.aichat.aichatdemo2.platform.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaveUserMessageService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final ChatProperties chatProperties;

    /**
     * Проверяет, что чат принадлежит пользователю.
     */
    public void requireOwned(UUID userId, UUID chatId) {
        findOwned(userId, chatId);
    }

    /**
     * Проверяет владельца, сохраняет сообщение пользователя и собирает историю для модели.
     */
    @Transactional
    public PreparedSendInternalState prepare(UUID userId, UUID chatId, String content) {
        log.debug("сохранение сообщения пользователя: вход userId={} chatId={}", userId, chatId);
        var chat = findOwned(userId, chatId);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        var message = messageRepository.insert(chatId, MessageRole.USER, content, now);
        PreparedSendInternalState prepared = PreparedSendInternalState.builder()
                .chatId(chatId)
                .userMessageId(message.getId())
                .userContent(content)
                .titleMissing(chat.getTitle() == null)
                .history(messageRepository.findLatest(chatId, chatProperties.getModelHistory()))
                .build();
        log.debug("сохранение сообщения пользователя: выход history={}", prepared.getHistory().size());
        return prepared;
    }

    /**
     * Удаляет сообщение пользователя, если ответ модели не сохранился.
     */
    @Transactional
    public void discard(UUID messageId) {
        log.debug("удаление сообщения пользователя: вход messageId={}", messageId);
        messageRepository.delete(messageId);
        log.debug("удаление сообщения пользователя: выход messageId={}", messageId);
    }

    private ChatInternalState findOwned(UUID userId, UUID chatId) {
        return chatRepository.findOwned(chatId, userId)
                .orElseThrow(() -> BusinessException.createNotFound("Chat not found"));
    }
}
