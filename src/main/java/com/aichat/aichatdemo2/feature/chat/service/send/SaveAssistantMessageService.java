package com.aichat.aichatdemo2.feature.chat.service.send;

import com.aichat.aichatdemo2.feature.chat.repository.ChatRepository;
import com.aichat.aichatdemo2.feature.chat.repository.MessageRepository;
import com.aichat.aichatdemo2.feature.chat.state.MessageInternalState;
import com.aichat.aichatdemo2.feature.chat.state.MessageRole;
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

    @Transactional
    public UUID save(UUID chatId, String content) {
        log.debug("save assistant message enter chatId={}", chatId);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        MessageInternalState message = messageRepository.insert(chatId, MessageRole.ASSISTANT, content, now);
        chatRepository.touch(chatId, now);
        log.debug("save assistant message exit messageId={}", message.getId());
        return message.getId();
    }
}
