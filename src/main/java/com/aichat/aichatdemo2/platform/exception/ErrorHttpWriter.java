package com.aichat.aichatdemo2.platform.exception;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ErrorHttpWriter {

    private final ObjectMapper objectMapper;

    public ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String code, String message, Map<String, Object> details) {
        return ResponseEntity.status(status).body(buildBody(code, message, details));
    }

    public void write(HttpServletResponse response, HttpStatus status, String code, String message) throws IOException {
        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), buildBody(code, message, Map.of()));
    }

    public ErrorResponse buildBody(String code, String message, Map<String, Object> details) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .details(details == null ? Map.of() : details)
                .build();
    }
}
