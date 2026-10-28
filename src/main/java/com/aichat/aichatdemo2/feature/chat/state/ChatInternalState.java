package com.aichat.aichatdemo2.feature.chat.state;

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
public class ChatInternalState {

    private UUID id;
    private UUID userId;
    private String title;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
