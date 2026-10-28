package com.aichat.aichatdemo2.support;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class StubAiConfig {

    @Bean
    @Primary
    public ChatModel stubChatModel() {
        return new StubChatModel();
    }
}
