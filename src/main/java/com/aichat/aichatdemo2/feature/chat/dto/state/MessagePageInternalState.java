package com.aichat.aichatdemo2.feature.chat.dto.state;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessagePageInternalState {

    private List<MessageInternalState> items;
    private String nextCursor;
}
