package com.aichat.aichatdemo2.feature.chat.service.send;

import com.aichat.aichatdemo2.feature.chat.ChatLimits;
import com.aichat.aichatdemo2.feature.chat.repository.ChatRepository;
import com.aichat.aichatdemo2.feature.chat.repository.MessageRepository;
import com.aichat.aichatdemo2.feature.chat.state.ChatInternalState;
import com.aichat.aichatdemo2.feature.chat.state.MessageRole;
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

    @Transactional
    public PreparedSendInternalState prepare(UUID userId, UUID chatId, String content) {
        log.debug("save user message enter userId={} chatId={}", userId, chatId);
        ChatInternalState chat = chatRepository.findOwned(chatId, userId)
                .orElseThrow(() -> BusinessException.notFound("Chat not found"));
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        messageRepository.insert(chatId, MessageRole.USER, content, now);
        chatRepository.touch(chatId, now);
        PreparedSendInternalState prepared = PreparedSendInternalState.builder()
                .chatId(chatId)
                .userContent(content)
                .titleMissing(chat.getTitle() == null)
                .history(messageRepository.findLatest(chatId, ChatLimits.MODEL_HISTORY))
                .build();
        log.debug("save user message exit history={}", prepared.getHistory().size());
        return prepared;
    }
}
