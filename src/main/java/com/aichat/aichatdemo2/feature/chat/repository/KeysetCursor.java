package com.aichat.aichatdemo2.feature.chat.repository;

import com.aichat.aichatdemo2.platform.exception.ValidationException;
import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

public final class KeysetCursor {

    private KeysetCursor() {
    }

    public static String encode(OffsetDateTime time, UUID id) {
        String raw = time.toInstant().toString() + "|" + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static Position decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            int split = raw.lastIndexOf('|');
            Instant instant = Instant.parse(raw.substring(0, split));
            return new Position(OffsetDateTime.ofInstant(instant, ZoneOffset.UTC), UUID.fromString(raw.substring(split + 1)));
        } catch (RuntimeException exception) {
            throw new ValidationException("Ошибка валидации", Map.of("cursor", "курсор составлен неверно"));
        }
    }

    @Getter
    public static final class Position {

        private final OffsetDateTime time;
        private final UUID id;

        public Position(OffsetDateTime time, UUID id) {
            this.time = time;
            this.id = id;
        }
    }
}
