package com.aichat.aichatdemo2.feature.chat.service;

import com.aichat.aichatdemo2.feature.chat.dto.web.response.ChatSummaryResponse;
import com.aichat.aichatdemo2.feature.chat.mapper.ChatAutoMapper;
import com.aichat.aichatdemo2.feature.chat.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchChatService {

    private final ChatRepository chatRepository;
    private final ChatAutoMapper chatAutoMapper;

    /**
     * Возвращает чаты пользователя. Непустой запрос фильтрует по заголовку.
     */
    @Transactional(readOnly = true)
    public List<ChatSummaryResponse> search(UUID userId, String query) {
        log.debug("поиск чатов: вход userId={} фильтр={}", userId, query != null && !query.isBlank());
        List<ChatSummaryResponse> chats = chatAutoMapper.toSummaries(chatRepository.search(userId, query));
        log.debug("поиск чатов: выход count={}", chats.size());
        return chats;
    }
}
