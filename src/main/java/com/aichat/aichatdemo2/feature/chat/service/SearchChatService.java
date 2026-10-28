package com.aichat.aichatdemo2.feature.chat.service;

import com.aichat.aichatdemo2.feature.chat.dto.web.response.ChatPageResponse;
import com.aichat.aichatdemo2.feature.chat.mapper.ChatAutoMapper;
import com.aichat.aichatdemo2.feature.chat.repository.ChatRepository;
import com.aichat.aichatdemo2.feature.chat.repository.KeysetCursor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchChatService {

    private final ChatRepository chatRepository;
    private final ChatAutoMapper chatAutoMapper;
    private final PageLimitResolverService pageLimitResolverService;

    /**
     * Возвращает страницу чатов пользователя. Непустой запрос фильтрует по заголовку.
     */
    @Transactional(readOnly = true)
    public ChatPageResponse search(UUID userId, String query, Integer limit, String cursor) {
        log.debug("поиск чатов: вход userId={} фильтр={}", userId, query != null && !query.isBlank());
        ChatPageResponse page = chatAutoMapper.mapToPage(chatRepository.search(
                userId, query, pageLimitResolverService.resolve(limit), KeysetCursor.decode(cursor)));
        log.debug("поиск чатов: выход count={}", page.getItems().size());
        return page;
    }
}
