package com.aichat.aichatdemo2.feature.chat;

import com.aichat.aichatdemo2.platform.util.ApiPaths;
import com.aichat.aichatdemo2.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatIntegrationTest extends AbstractIntegrationTest {

    @Test
    void createsListsAndHidesForeignChats() throws Exception {
        String owner = register(loginOf(UUID.randomUUID().toString()));
        String other = register(loginOf(UUID.randomUUID().toString()));
        String chatId = createChat(owner);

        mockMvc.perform(get(ApiPaths.Chats.BASE).header(AUTHORIZATION, "Bearer " + owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(chatId))
                .andExpect(jsonPath("$.items[0].title").value(nullValue()))
                .andExpect(jsonPath("$.nextCursor").value(nullValue()));
        mockMvc.perform(get(ApiPaths.Chats.BASE).param("cursor", "bad").header(AUTHORIZATION, "Bearer " + owner))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value("Ошибка валидации"))
                .andExpect(jsonPath("$.details.cursor").value("курсор составлен неверно"));
        mockMvc.perform(get(ApiPaths.Chats.buildChatPath(chatId)).header(AUTHORIZATION, "Bearer " + owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdAt", endsWith("Z")))
                .andExpect(jsonPath("$.updatedAt", endsWith("Z")))
                .andExpect(jsonPath("$.messages.length()").value(0));
        mockMvc.perform(get(ApiPaths.Chats.buildChatPath(chatId)).header(AUTHORIZATION, "Bearer " + other))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    private String createChat(String token) throws Exception {
        var result = mockMvc.perform(post(ApiPaths.Chats.BASE).header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asString();
    }
}
