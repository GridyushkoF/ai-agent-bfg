package com.aichat.aichatdemo2.feature.chat.service;

import com.aichat.aichatdemo2.feature.chat.ChatProperties;
import com.aichat.aichatdemo2.platform.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PageLimitResolverService {

    private final ChatProperties chatProperties;

    /**
     * Возвращает размер страницы из запроса или значение по умолчанию.
     */
    public int resolve(Integer requested) {
        int max = chatProperties.getMaxPageSize();
        if (requested == null) {
            return chatProperties.getPageSize();
        }
        if (requested < 1 || requested > max) {
            throw new ValidationException("Ошибка валидации", Map.of("limit", "размер должен быть от 1 до " + max));
        }
        return requested;
    }
}
