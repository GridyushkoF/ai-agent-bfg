package com.aichat.aichatdemo2.platform.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class AppPrincipal {

    private final UUID userId;
    private final String login;
}
