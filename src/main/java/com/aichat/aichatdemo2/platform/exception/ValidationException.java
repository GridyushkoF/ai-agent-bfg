package com.aichat.aichatdemo2.platform.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ValidationException extends AppException {

    public ValidationException(String message, Map<String, Object> details) {
        super(ErrorCodes.VALIDATION, message, HttpStatus.BAD_REQUEST, details);
    }
}
