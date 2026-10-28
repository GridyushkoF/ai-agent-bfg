package com.aichat.aichatdemo2.feature.chat.state;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatViewInternalState {

    private ChatInternalState chat;
    private List<MessageInternalState> messages;
}
