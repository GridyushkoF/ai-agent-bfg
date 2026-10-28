package com.aichat.aichatdemo2.feature.user.controller;

import com.aichat.aichatdemo2.feature.user.dto.web.response.UserResponse;
import com.aichat.aichatdemo2.feature.user.service.CurrentUserService;
import com.aichat.aichatdemo2.platform.config.OpenApiConfig;
import com.aichat.aichatdemo2.platform.security.CurrentUser;
import com.aichat.aichatdemo2.platform.util.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(ApiPaths.Users.BASE)
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.BEARER)
@Tag(name = "Пользователь", description = "Профиль текущего пользователя")
public class UserController {

    private final CurrentUserService currentUserService;
    private final CurrentUser currentUser;

    @GetMapping(ApiPaths.Users.ME)
    @Operation(summary = "Текущий пользователь", description = "Возвращает профиль по JWT из заголовка Authorization.")
    public UserResponse me() {
        return currentUserService.me(currentUser.id());
    }
}
