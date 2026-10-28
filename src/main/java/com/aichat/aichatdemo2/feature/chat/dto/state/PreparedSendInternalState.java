package com.aichat.aichatdemo2.feature.chat.dto.state;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreparedSendInternalState {

    private UUID chatId;
    private String userContent;
    private boolean titleMissing;
    private List<MessageInternalState> history;
}
