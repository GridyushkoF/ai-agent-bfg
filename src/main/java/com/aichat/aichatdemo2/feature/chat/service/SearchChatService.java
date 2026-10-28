package com.aichat.aichatdemo2.feature.chat.service;

import com.aichat.aichatdemo2.feature.chat.repository.ChatRepository;
import com.aichat.aichatdemo2.feature.chat.state.ChatInternalState;
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

    @Transactional(readOnly = true)
    public List<ChatInternalState> search(UUID userId, String query) {
        log.debug("search chats enter userId={} filtered={}", userId, query != null && !query.isBlank());
        List<ChatInternalState> chats = chatRepository.search(userId, query);
        log.debug("search chats exit count={}", chats.size());
        return chats;
    }
}
