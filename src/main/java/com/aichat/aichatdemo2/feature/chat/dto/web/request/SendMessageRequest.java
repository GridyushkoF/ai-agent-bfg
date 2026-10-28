package com.aichat.aichatdemo2.feature.chat.dto.web.request;

import jakarta.validation.constraints.NotBlank;
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
    @MessageContent
    private String content;
}
