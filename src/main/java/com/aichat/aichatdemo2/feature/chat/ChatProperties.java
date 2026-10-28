package com.aichat.aichatdemo2.feature.chat;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.chat")
public class ChatProperties {

    @Min(1)
    private int modelHistory = 40;

    @Min(1)
    private int titleMax = 120;

    @Min(1)
    private int messageMax = 16000;
}
