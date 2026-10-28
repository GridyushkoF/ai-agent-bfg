package com.aichat.aichatdemo2.feature.chat.service;

import com.aichat.aichatdemo2.feature.chat.dto.state.ChatViewInternalState;
import com.aichat.aichatdemo2.feature.chat.dto.state.MessagePageInternalState;
import com.aichat.aichatdemo2.feature.chat.dto.web.response.ChatDetailsResponse;
import com.aichat.aichatdemo2.feature.chat.mapper.ChatAutoMapper;
import com.aichat.aichatdemo2.feature.chat.repository.ChatRepository;
import com.aichat.aichatdemo2.feature.chat.repository.KeysetCursor;
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
    private final PageLimitResolverService pageLimitResolverService;

    /**
     * Возвращает чат пользователя и страницу последних сообщений.
     */
    @Transactional(readOnly = true)
    public ChatDetailsResponse get(UUID userId, UUID chatId, Integer limit, String cursor) {
        log.debug("чтение чата: вход userId={} chatId={}", userId, chatId);
        var chat = chatRepository.findOwned(chatId, userId)
                .orElseThrow(() -> BusinessException.createNotFound("Chat not found"));
        MessagePageInternalState page = messageRepository.findPage(
                chatId, pageLimitResolverService.resolve(limit), KeysetCursor.decode(cursor));
        ChatViewInternalState view = ChatViewInternalState.builder()
                .chat(chat)
                .messages(page.getItems())
                .nextCursor(page.getNextCursor())
                .build();
        log.debug("чтение чата: выход messages={}", view.getMessages().size());
        return chatAutoMapper.mapToDetails(view);
    }
}
