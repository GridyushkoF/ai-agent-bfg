package com.aichat.aichatdemo2.feature.chat.service;

import com.aichat.aichatdemo2.feature.chat.repository.ChatRepository;
import com.aichat.aichatdemo2.feature.chat.repository.MessageRepository;
import com.aichat.aichatdemo2.feature.chat.state.ChatInternalState;
import com.aichat.aichatdemo2.feature.chat.state.ChatViewInternalState;
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

    @Transactional(readOnly = true)
    public ChatViewInternalState get(UUID userId, UUID chatId) {
        log.debug("get chat enter userId={} chatId={}", userId, chatId);
        ChatInternalState chat = chatRepository.findOwned(chatId, userId)
                .orElseThrow(() -> BusinessException.notFound("Chat not found"));
        ChatViewInternalState view = ChatViewInternalState.builder()
                .chat(chat)
                .messages(messageRepository.findByChat(chatId))
                .build();
        log.debug("get chat exit messages={}", view.getMessages().size());
        return view;
    }
}
