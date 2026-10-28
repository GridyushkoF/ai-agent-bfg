package com.aichat.aichatdemo2.feature.chat.repository;

import com.aichat.aichatdemo2.feature.chat.dto.state.ChatInternalState;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static com.aichat.aichatdemo2.jooq.tables.Chat.CHAT;

@Repository
@RequiredArgsConstructor
public class ChatRepository {

    private final DSLContext dsl;

    public ChatInternalState insert(UUID userId) {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        dsl.insertInto(CHAT)
                .set(CHAT.ID, id)
                .set(CHAT.USER_ID, userId)
                .set(CHAT.CREATED_AT, now)
                .set(CHAT.UPDATED_AT, now)
                .execute();
        return ChatInternalState.builder().id(id).userId(userId).createdAt(now).updatedAt(now).build();
    }

    public Optional<ChatInternalState> findOwned(UUID chatId, UUID userId) {
        return dsl.selectFrom(CHAT)
                .where(CHAT.ID.eq(chatId).and(CHAT.USER_ID.eq(userId)))
                .fetchOptional(this::map);
    }

    public List<ChatInternalState> search(UUID userId, String query) {
        Condition condition = CHAT.USER_ID.eq(userId);
        if (query != null && !query.isBlank()) {
            condition = condition.and(CHAT.TITLE.isNotNull()).and(DSL.lower(CHAT.TITLE).like(likePattern(query), '\\'));
        }
        return dsl.selectFrom(CHAT)
                .where(condition)
                .orderBy(CHAT.UPDATED_AT.desc(), CHAT.ID.desc())
                .fetch(this::map);
    }

    public void touch(UUID chatId, OffsetDateTime updatedAt) {
        dsl.update(CHAT).set(CHAT.UPDATED_AT, updatedAt).where(CHAT.ID.eq(chatId)).execute();
    }

    public boolean setTitleIfAbsent(UUID chatId, String title) {
        return dsl.update(CHAT)
                .set(CHAT.TITLE, title)
                .where(CHAT.ID.eq(chatId).and(CHAT.TITLE.isNull()))
                .execute() == 1;
    }

    private ChatInternalState map(Record record) {
        return ChatInternalState.builder()
                .id(record.get(CHAT.ID))
                .userId(record.get(CHAT.USER_ID))
                .title(record.get(CHAT.TITLE))
                .createdAt(record.get(CHAT.CREATED_AT))
                .updatedAt(record.get(CHAT.UPDATED_AT))
                .build();
    }

    private static String likePattern(String query) {
        String escaped = query.trim().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
