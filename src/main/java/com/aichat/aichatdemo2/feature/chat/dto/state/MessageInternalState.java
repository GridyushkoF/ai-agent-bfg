package com.aichat.aichatdemo2.feature.chat.dto.state;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageInternalState {

    private UUID id;
    private UUID chatId;
    private MessageRole role;
    @ToString.Exclude
    private String content;
    private OffsetDateTime createdAt;
}
