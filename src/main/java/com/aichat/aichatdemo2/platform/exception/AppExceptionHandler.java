package com.aichat.aichatdemo2.platform.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
@RequiredArgsConstructor
public class AppExceptionHandler {

    private final ErrorHttpWriter errors;

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleApp(AppException exception) {
        if (exception instanceof TechnicalException) {
            log.error("technical failure code={}", exception.getCode(), exception);
        } else {
            log.debug("request failed code={} message={}", exception.getCode(), exception.getMessage());
        }
        return errors.entity(exception.getStatus(), exception.getCode(), exception.getMessage(), exception.getDetails());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
        log.error("unexpected failure", exception);
        return errors.entity(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.TECHNICAL, "Internal error", Map.of());
    }
}
