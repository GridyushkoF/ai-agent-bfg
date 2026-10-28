package com.aichat.aichatdemo2.feature.user.controller;

import com.aichat.aichatdemo2.feature.user.dto.web.request.LoginRequest;
import com.aichat.aichatdemo2.feature.user.dto.web.request.RegisterRequest;
import com.aichat.aichatdemo2.feature.user.dto.web.response.AuthResponse;
import com.aichat.aichatdemo2.feature.user.service.LoginUserService;
import com.aichat.aichatdemo2.feature.user.service.RegisterUserService;
import com.aichat.aichatdemo2.platform.util.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(ApiPaths.Auth.BASE)
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "Регистрация и вход по логину и паролю")
public class AuthController {

    private final RegisterUserService registerUserService;
    private final LoginUserService loginUserService;

    @PostMapping(ApiPaths.Auth.REGISTER)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Регистрация", description = "Создаёт пользователя и возвращает JWT. Логин и почта сохраняются в нижнем регистре.")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return registerUserService.register(request);
    }

    @PostMapping(ApiPaths.Auth.LOGIN)
    @Operation(summary = "Вход", description = "Проверяет логин и пароль и возвращает JWT.")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return loginUserService.login(request);
    }
}
