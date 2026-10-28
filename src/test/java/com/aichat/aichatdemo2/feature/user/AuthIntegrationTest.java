package com.aichat.aichatdemo2.feature.user;

import com.aichat.aichatdemo2.feature.user.dto.web.request.LoginRequest;
import com.aichat.aichatdemo2.feature.user.dto.web.request.RegisterRequest;
import com.aichat.aichatdemo2.platform.util.ApiPaths;
import com.aichat.aichatdemo2.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends AbstractIntegrationTest {

    @Test
    void registersLogsInAndReadsProfile() throws Exception {
        String login = loginOf(UUID.randomUUID().toString());
        String token = register(login);

        mockMvc.perform(post(ApiPaths.Auth.login())
                        .contentType(APPLICATION_JSON)
                        .content(writeJson(LoginRequest.builder().login(login.toUpperCase()).password("password123").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.login").value(login))
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist());

        mockMvc.perform(get(ApiPaths.Users.buildProfilePath()).header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(login + "@example.com"));
    }

    @Test
    void rejectsDuplicateLoginBadPasswordAndAnonymous() throws Exception {
        String login = loginOf(UUID.randomUUID().toString());
        register(login);

        mockMvc.perform(post(ApiPaths.Auth.register()).contentType(APPLICATION_JSON).content(writeJson(buildRegisterRequest(login))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("LOGIN_TAKEN"));
        mockMvc.perform(post(ApiPaths.Auth.login())
                        .contentType(APPLICATION_JSON)
                        .content(writeJson(LoginRequest.builder().login(login).password("wrong-password").build())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        mockMvc.perform(get(ApiPaths.Users.buildProfilePath()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post(ApiPaths.Auth.register())
                        .contentType(APPLICATION_JSON)
                        .content(writeJson(RegisterRequest.builder().login("ab").email("bad").password("short").build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value("Ошибка валидации"))
                .andExpect(jsonPath("$.details.login").value("длина должна быть от 3 до 64"))
                .andExpect(jsonPath("$.details.email").value("должно быть корректным адресом электронной почты"))
                .andExpect(jsonPath("$.details.password").value("длина должна быть от 8 до 72"));
    }

    @Test
    void rejectsTakenEmail() throws Exception {
        String login = loginOf(UUID.randomUUID().toString());
        register(login);
        String other = loginOf(UUID.randomUUID().toString());
        mockMvc.perform(post(ApiPaths.Auth.register())
                        .contentType(APPLICATION_JSON)
                        .content(writeJson(RegisterRequest.builder()
                                .login(other)
                                .email(login + "@example.com")
                                .password("password123")
                                .build())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_TAKEN"));
    }
}
