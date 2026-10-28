package com.aichat.aichatdemo2.feature.chat;

import jakarta.validation.constraints.Max;
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

    public static final int TITLE_COLUMN_LIMIT = 120;

    @Min(1)
    private int modelHistory = 40;

    @Min(1)
    @Max(value = TITLE_COLUMN_LIMIT, message = "не должно быть больше {value}")
    private int titleMax = TITLE_COLUMN_LIMIT;

    @Min(1)
    private int messageMax = 16000;

    @Min(1)
    private int pageSize = 20;

    @Min(1)
    private int maxPageSize = 100;
}
