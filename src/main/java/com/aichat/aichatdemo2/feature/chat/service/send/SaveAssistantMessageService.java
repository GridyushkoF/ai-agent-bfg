package com.aichat.aichatdemo2.feature.chat.service.send;

import com.aichat.aichatdemo2.feature.chat.dto.state.MessageInternalState;
import com.aichat.aichatdemo2.feature.chat.dto.enumerated.MessageRole;
import com.aichat.aichatdemo2.feature.chat.repository.ChatRepository;
import com.aichat.aichatdemo2.feature.chat.repository.MessageRepository;
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
public class SaveAssistantMessageService {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;

    /**
     * Сохраняет ответ бота и обновляет время чата.
     */
    @Transactional
    public UUID save(UUID chatId, String content) {
        log.debug("сохранение ответа: вход chatId={}", chatId);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        MessageInternalState message = messageRepository.insert(chatId, MessageRole.ASSISTANT, content, now);
        chatRepository.touch(chatId, now);
        log.debug("сохранение ответа: выход messageId={}", message.getId());
        return message.getId();
    }
}
