package com.aichat.aichatdemo2.feature.chat.service;

import com.aichat.aichatdemo2.feature.chat.repository.ChatRepository;
import com.aichat.aichatdemo2.feature.chat.state.ChatInternalState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateChatService {

    private final ChatRepository chatRepository;

    @Transactional
    public ChatInternalState create(UUID userId) {
        log.debug("create chat enter userId={}", userId);
        ChatInternalState chat = chatRepository.insert(userId);
        log.debug("create chat exit chatId={}", chat.getId());
        return chat;
    }
}
