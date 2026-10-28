package com.aichat.aichatdemo2.feature.chat.validation;

import com.aichat.aichatdemo2.feature.chat.ChatProperties;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MessageContentValidator implements ConstraintValidator<MessageContent, String> {

    private final ChatProperties chatProperties;

    public MessageContentValidator(ChatProperties chatProperties) {
        this.chatProperties = chatProperties;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.length() <= chatProperties.getMessageMax()) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("длина должна быть не больше " + chatProperties.getMessageMax())
                .addConstraintViolation();
        return false;
    }
}
