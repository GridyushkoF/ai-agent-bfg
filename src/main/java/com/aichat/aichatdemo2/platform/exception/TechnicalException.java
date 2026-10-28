package com.aichat.aichatdemo2.platform.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class TechnicalException extends AppException {

    public TechnicalException(String message, Throwable cause) {
        super(ErrorCodes.TECHNICAL, message, HttpStatus.INTERNAL_SERVER_ERROR, Map.of());
        if (cause != null) {
            initCause(cause);
        }
    }
}
