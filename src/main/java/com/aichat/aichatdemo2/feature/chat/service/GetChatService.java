package com.aichat.aichatdemo2.feature.chat.service;

import com.aichat.aichatdemo2.feature.chat.dto.state.ChatViewInternalState;
import com.aichat.aichatdemo2.feature.chat.dto.web.response.ChatDetailsResponse;
import com.aichat.aichatdemo2.feature.chat.mapper.ChatAutoMapper;
import com.aichat.aichatdemo2.feature.chat.repository.ChatRepository;
import com.aichat.aichatdemo2.feature.chat.repository.MessageRepository;
import com.aichat.aichatdemo2.platform.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetChatService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final ChatAutoMapper chatAutoMapper;

    /**
     * Возвращает чат пользователя вместе с сообщениями.
     */
    @Transactional(readOnly = true)
    public ChatDetailsResponse get(UUID userId, UUID chatId) {
        log.debug("чтение чата: вход userId={} chatId={}", userId, chatId);
        var chat = chatRepository.findOwned(chatId, userId)
                .orElseThrow(() -> BusinessException.notFound("Chat not found"));
        ChatViewInternalState view = ChatViewInternalState.builder()
                .chat(chat)
                .messages(messageRepository.findByChat(chatId))
                .build();
        log.debug("чтение чата: выход messages={}", view.getMessages().size());
        return chatAutoMapper.toDetails(view);
    }
}
