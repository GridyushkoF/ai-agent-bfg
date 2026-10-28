package com.aichat.aichatdemo2.feature.chat.dto;

import com.aichat.aichatdemo2.feature.chat.ChatLimits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Data
@Builder
@Validated
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    @NotBlank
    @Size(max = ChatLimits.MESSAGE_MAX)
    private String content;
}
