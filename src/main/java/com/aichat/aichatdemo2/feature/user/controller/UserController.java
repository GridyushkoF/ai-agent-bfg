package com.aichat.aichatdemo2.feature.user.controller;

import com.aichat.aichatdemo2.feature.user.dto.UserResponse;
import com.aichat.aichatdemo2.feature.user.mapper.UserAutoMapper;
import com.aichat.aichatdemo2.feature.user.service.CurrentUserService;
import com.aichat.aichatdemo2.platform.config.OpenApiConfig;
import com.aichat.aichatdemo2.platform.security.CurrentUser;
import com.aichat.aichatdemo2.platform.util.ApiPaths;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(ApiPaths.USERS)
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.BEARER)
public class UserController {

    private final CurrentUserService currentUserService;
    private final CurrentUser currentUser;
    private final UserAutoMapper userAutoMapper;

    @GetMapping("/me")
    public UserResponse me() {
        return userAutoMapper.toUser(currentUserService.me(currentUser.id()));
    }
}
