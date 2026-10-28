package com.aichat.aichatdemo2.feature.chat.controller;

import com.aichat.aichatdemo2.feature.chat.dto.web.request.SendMessageRequest;
import com.aichat.aichatdemo2.feature.chat.dto.web.response.ChatDetailsResponse;
import com.aichat.aichatdemo2.feature.chat.dto.web.response.ChatSummaryResponse;
import com.aichat.aichatdemo2.feature.chat.service.CreateChatService;
import com.aichat.aichatdemo2.feature.chat.service.GetChatService;
import com.aichat.aichatdemo2.feature.chat.service.SearchChatService;
import com.aichat.aichatdemo2.feature.chat.service.send.SendMessageFacade;
import com.aichat.aichatdemo2.platform.config.OpenApiConfig;
import com.aichat.aichatdemo2.platform.security.CurrentUser;
import com.aichat.aichatdemo2.platform.util.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping(ApiPaths.Chats.BASE)
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.BEARER)
@Tag(name = "Чаты", description = "Список чатов, переключение и сообщения боту")
public class ChatController {

    private final CreateChatService createChatService;
    private final SearchChatService searchChatService;
    private final GetChatService getChatService;
    private final SendMessageFacade sendMessageFacade;
    private final CurrentUser currentUser;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать чат", description = "Создаёт пустой чат текущего пользователя. Заголовок появится после первого сообщения.")
    public ChatSummaryResponse create() {
        return createChatService.create(currentUser.id());
    }

    @GetMapping
    @Operation(summary = "Список чатов", description = "Возвращает чаты пользователя. Непустой query ищет по заголовку без учёта регистра.")
    public List<ChatSummaryResponse> list(
            @Parameter(description = "Подстрока заголовка. Пустое значение возвращает все чаты.")
            @RequestParam(name = "query", required = false) String query) {
        return searchChatService.search(currentUser.id(), query);
    }

    @GetMapping(ApiPaths.Chats.BY_ID)
    @Operation(summary = "Открыть чат", description = "Возвращает чат и полную историю сообщений. Чужой чат отвечает как ненайденный.")
    public ChatDetailsResponse get(@Parameter(description = "Идентификатор чата") @PathVariable UUID chatId) {
        return getChatService.get(currentUser.id(), chatId);
    }

    @PostMapping(value = ApiPaths.Chats.MESSAGES, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Отправить сообщение", description = "Пишет сообщение пользователя и стримит ответ бота в Markdown. События SSE: delta, title, done, error.")
    public SseEmitter send(
            @Parameter(description = "Идентификатор чата") @PathVariable UUID chatId,
            @Valid @RequestBody SendMessageRequest request) {
        return sendMessageFacade.send(currentUser.id(), chatId, request);
    }
}
