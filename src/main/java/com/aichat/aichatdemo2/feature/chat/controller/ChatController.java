package com.aichat.aichatdemo2.feature.chat.controller;

import com.aichat.aichatdemo2.feature.chat.dto.ChatDetailsResponse;
import com.aichat.aichatdemo2.feature.chat.dto.ChatSummaryResponse;
import com.aichat.aichatdemo2.feature.chat.dto.SendMessageRequest;
import com.aichat.aichatdemo2.feature.chat.mapper.ChatAutoMapper;
import com.aichat.aichatdemo2.feature.chat.service.CreateChatService;
import com.aichat.aichatdemo2.feature.chat.service.GetChatService;
import com.aichat.aichatdemo2.feature.chat.service.SearchChatService;
import com.aichat.aichatdemo2.feature.chat.service.send.SendMessageFacade;
import com.aichat.aichatdemo2.platform.config.OpenApiConfig;
import com.aichat.aichatdemo2.platform.security.CurrentUser;
import com.aichat.aichatdemo2.platform.util.ApiPaths;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping(ApiPaths.CHATS)
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.BEARER)
public class ChatController {

    private final CreateChatService createChatService;
    private final SearchChatService searchChatService;
    private final GetChatService getChatService;
    private final SendMessageFacade sendMessageFacade;
    private final ChatAutoMapper chatAutoMapper;
    private final CurrentUser currentUser;

    @PostMapping
    public ResponseEntity<ChatSummaryResponse> create() {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatAutoMapper.toSummary(createChatService.create(currentUser.id())));
    }

    @GetMapping
    public List<ChatSummaryResponse> list(@RequestParam(name = "query", required = false) String query) {
        return chatAutoMapper.toSummaries(searchChatService.search(currentUser.id(), query));
    }

    @GetMapping("/{chatId}")
    public ChatDetailsResponse get(@PathVariable UUID chatId) {
        return chatAutoMapper.toDetails(getChatService.get(currentUser.id(), chatId));
    }

    @PostMapping(value = "/{chatId}/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter send(@PathVariable UUID chatId, @Valid @RequestBody SendMessageRequest request) {
        return sendMessageFacade.send(currentUser.id(), chatId, request);
    }
}
