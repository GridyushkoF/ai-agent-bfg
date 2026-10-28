package com.aichat.aichatdemo2.feature.chat.service.send;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatSseWriter {

    static final String DELTA = "delta";
    static final String TITLE = "title";
    static final String DONE = "done";
    static final String ERROR = "error";

    private final ObjectMapper objectMapper;

    public SseEmitter open() {
        SseEmitter emitter = new SseEmitter(Duration.ofMinutes(5).toMillis());
        emitter.onTimeout(emitter::complete);
        return emitter;
    }

    public void send(SseEmitter emitter, String event, Map<String, ?> payload) {
        try {
            emitter.send(SseEmitter.event().name(event).data(objectMapper.writeValueAsString(payload)));
        } catch (IOException exception) {
            throw new ClientDisconnectedException();
        }
    }

    public void completeWithError(SseEmitter emitter) {
        try {
            send(emitter, ERROR, Map.of("message", "AI service is unavailable"));
            emitter.complete();
        } catch (ClientDisconnectedException exception) {
            emitter.complete();
        }
    }

    static final class ClientDisconnectedException extends RuntimeException {
    }
}
