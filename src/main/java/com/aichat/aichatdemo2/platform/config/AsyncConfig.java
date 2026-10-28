package com.aichat.aichatdemo2.platform.config;

import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AsyncConfig {

    private final ExecutorService chatStreams = Executors.newVirtualThreadPerTaskExecutor();

    @Bean
    public TaskExecutor chatStreamExecutor() {
        return chatStreams::execute;
    }

    @PreDestroy
    public void shutdownChatStreams() {
        chatStreams.shutdown();
    }
}
