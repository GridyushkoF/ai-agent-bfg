package com.aichat.aichatdemo2.platform.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends AppException {

    public BusinessException(String code, String message, HttpStatus status) {
        super(code, message, status, java.util.Map.of());
    }

    public static BusinessException createNotFound(String message) {
        return new BusinessException(ErrorCodes.NOT_FOUND, message, HttpStatus.NOT_FOUND);
    }

    public static BusinessException createConflict(String code, String message) {
        return new BusinessException(code, message, HttpStatus.CONFLICT);
    }

    public static BusinessException createUnauthorized(String message) {
        return new BusinessException(ErrorCodes.UNAUTHORIZED, message, HttpStatus.UNAUTHORIZED);
    }
}
