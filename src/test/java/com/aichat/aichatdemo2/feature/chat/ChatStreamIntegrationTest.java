package com.aichat.aichatdemo2.feature.chat;

import com.aichat.aichatdemo2.platform.util.ApiPaths;
import com.aichat.aichatdemo2.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatStreamIntegrationTest extends AbstractIntegrationTest {

    @Test
    void streamsMarkdownAndGeneratesTitleOnce() throws Exception {
        String token = register(loginOf(UUID.randomUUID().toString()));
        String chatId = mockMvc.perform(post(ApiPaths.Chats.BASE).header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        chatId = objectMapper.readTree(chatId).get("id").asString();

        String first = stream(token, chatId, "Alpha reactors");
        assertThat(first).contains("event:delta", "Hello from the bot", "event:title", "Alpha reactors", "event:done");

        mockMvc.perform(get(ApiPaths.Chats.chat(chatId)).header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Alpha reactors"))
                .andExpect(jsonPath("$.messages.length()").value(2))
                .andExpect(jsonPath("$.messages[0].role").value("USER"))
                .andExpect(jsonPath("$.messages[1].role").value("ASSISTANT"))
                .andExpect(jsonPath("$.messages[1].content").value("## Answer\n\nHello from the bot."));

        String second = stream(token, chatId, "Beta follow up");
        assertThat(second).doesNotContain("event:title");
        mockMvc.perform(get(ApiPaths.Chats.BASE).param("query", "alpha").header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get(ApiPaths.Chats.BASE).param("query", "missing").header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private String stream(String token, String chatId, String content) throws Exception {
        MvcResult started = mockMvc.perform(post(ApiPaths.Chats.messages(chatId))
                        .header(AUTHORIZATION, "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .accept(TEXT_EVENT_STREAM)
                        .content("{\"content\":\"" + content + "\"}"))
                .andExpect(request().asyncStarted())
                .andReturn();
        return mockMvc.perform(asyncDispatch(started))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }
}
