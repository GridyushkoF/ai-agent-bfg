package com.aichat.aichatdemo2.feature.chat.service;

import com.aichat.aichatdemo2.feature.chat.dto.web.response.ChatSummaryResponse;
import com.aichat.aichatdemo2.feature.chat.mapper.ChatAutoMapper;
import com.aichat.aichatdemo2.feature.chat.repository.ChatRepository;
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
    private final ChatAutoMapper chatAutoMapper;

    /**
     * Создаёт пустой чат пользователя.
     */
    @Transactional
    public ChatSummaryResponse create(UUID userId) {
        log.debug("создание чата: вход userId={}", userId);
        var chat = chatRepository.insert(userId);
        log.debug("создание чата: выход chatId={}", chat.getId());
        return chatAutoMapper.toSummary(chat);
    }
}
