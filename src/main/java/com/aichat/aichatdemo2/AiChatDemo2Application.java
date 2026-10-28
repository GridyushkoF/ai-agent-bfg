package com.aichat.aichatdemo2;

import com.aichat.aichatdemo2.feature.chat.ChatProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ChatProperties.class)
public class AiChatDemo2Application {

    static void main(String[] args) {
        SpringApplication.run(AiChatDemo2Application.class, args);
    }

}
