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
            log.error("техническая ошибка code={}", exception.getCode(), exception);
        } else {
            log.debug("запрос отклонён code={} message={}", exception.getCode(), exception.getMessage());
        }
        return errors.buildResponse(exception.getStatus(), exception.getCode(), exception.getMessage(), exception.getDetails());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
        log.error("непредвиденная ошибка", exception);
        return errors.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.TECHNICAL, "Internal error", Map.of());
    }
}
