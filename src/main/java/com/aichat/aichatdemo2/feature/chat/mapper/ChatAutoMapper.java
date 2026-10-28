package com.aichat.aichatdemo2.feature.chat.mapper;

import com.aichat.aichatdemo2.feature.chat.dto.state.ChatInternalState;
import com.aichat.aichatdemo2.feature.chat.dto.state.ChatViewInternalState;
import com.aichat.aichatdemo2.feature.chat.dto.state.MessageInternalState;
import com.aichat.aichatdemo2.feature.chat.dto.web.response.ChatDetailsResponse;
import com.aichat.aichatdemo2.feature.chat.dto.web.response.ChatSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ChatAutoMapper {

    ChatSummaryResponse toSummary(ChatInternalState state);

    List<ChatSummaryResponse> toSummaries(List<ChatInternalState> states);

    @Mapping(target = "role", expression = "java(state.getRole().name())")
    ChatDetailsResponse.MessageItem toMessage(MessageInternalState state);

    default ChatDetailsResponse toDetails(ChatViewInternalState view) {
        ChatInternalState chat = view.getChat();
        return ChatDetailsResponse.builder()
                .id(chat.getId())
                .title(chat.getTitle())
                .createdAt(chat.getCreatedAt())
                .updatedAt(chat.getUpdatedAt())
                .messages(view.getMessages().stream().map(this::toMessage).toList())
                .build();
    }
}
