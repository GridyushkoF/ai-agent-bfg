package com.aichat.aichatdemo2.feature.chat.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = MessageContentValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MessageContent {

    String message() default "сообщение слишком длинное";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
