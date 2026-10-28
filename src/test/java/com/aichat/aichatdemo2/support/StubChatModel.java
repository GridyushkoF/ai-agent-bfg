package com.aichat.aichatdemo2.support;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.List;

public class StubChatModel implements ChatModel {

    @Override
    public ChatResponse call(Prompt prompt) {
        String system = prompt.getSystemMessage() == null ? "" : prompt.getSystemMessage().getText();
        String user = prompt.getUserMessage() == null ? "" : prompt.getUserMessage().getText();
        String text = system.contains("title") ? user.trim() : "unused";
        if (text.isEmpty()) {
            text = "New chat";
        }
        if (text.length() > 120) {
            text = text.substring(0, 120);
        }
        return reply(text);
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        String user = prompt.getUserMessage() == null ? "" : prompt.getUserMessage().getText();
        if ("fail-the-model".equals(user)) {
            return Flux.error(new IllegalStateException("model down"));
        }
        return Flux.just("## Answer\n\n", "Hello from the bot.").map(StubChatModel::reply);
    }

    private static ChatResponse reply(String text) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
    }
}
