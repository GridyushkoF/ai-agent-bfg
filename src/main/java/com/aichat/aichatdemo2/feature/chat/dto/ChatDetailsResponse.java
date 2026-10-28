package com.aichat.aichatdemo2.feature.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatDetailsResponse {

    private UUID id;
    private String title;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<MessageItem> messages;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageItem {

        private UUID id;
        private String role;
        private String content;
        private OffsetDateTime createdAt;
    }
}
