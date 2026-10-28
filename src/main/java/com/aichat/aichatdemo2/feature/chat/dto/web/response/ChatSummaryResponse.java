package com.aichat.aichatdemo2.feature.chat.dto.web.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSummaryResponse {

    private UUID id;
    private String title;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
