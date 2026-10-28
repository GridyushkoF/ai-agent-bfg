package com.aichat.aichatdemo2.feature.user.controller;

import com.aichat.aichatdemo2.feature.user.dto.AuthResponse;
import com.aichat.aichatdemo2.feature.user.dto.LoginRequest;
import com.aichat.aichatdemo2.feature.user.dto.RegisterRequest;
import com.aichat.aichatdemo2.feature.user.mapper.UserAutoMapper;
import com.aichat.aichatdemo2.feature.user.service.LoginUserService;
import com.aichat.aichatdemo2.feature.user.service.RegisterUserService;
import com.aichat.aichatdemo2.platform.util.ApiPaths;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(ApiPaths.AUTH)
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserService registerUserService;
    private final LoginUserService loginUserService;
    private final UserAutoMapper userAutoMapper;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userAutoMapper.toAuth(registerUserService.register(request)));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return userAutoMapper.toAuth(loginUserService.login(request));
    }
}
