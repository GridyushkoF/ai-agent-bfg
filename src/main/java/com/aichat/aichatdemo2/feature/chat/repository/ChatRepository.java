package com.aichat.aichatdemo2.feature.chat.repository;

import com.aichat.aichatdemo2.feature.chat.ChatProperties;
import com.aichat.aichatdemo2.feature.chat.dto.state.ChatInternalState;
import com.aichat.aichatdemo2.feature.chat.dto.state.ChatPageInternalState;
import com.aichat.aichatdemo2.platform.util.UtcTime;
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

    public ChatPageInternalState search(UUID userId, String query, int limit, KeysetCursor.Position cursor) {
        List<ChatInternalState> fetched = dsl.selectFrom(CHAT)
                .where(condition(userId, query, cursor))
                .orderBy(CHAT.UPDATED_AT.desc(), CHAT.ID.desc())
                .limit(limit + 1)
                .fetch(this::map);
        PageSlice<ChatInternalState> slice = PageSlice.take(
                fetched, limit, chat -> KeysetCursor.encode(chat.getUpdatedAt(), chat.getId()));
        return ChatPageInternalState.builder().items(slice.getItems()).nextCursor(slice.getNextCursor()).build();
    }

    public void touch(UUID chatId, OffsetDateTime updatedAt) {
        dsl.update(CHAT).set(CHAT.UPDATED_AT, updatedAt).where(CHAT.ID.eq(chatId)).execute();
    }

    public boolean setTitleIfAbsent(UUID chatId, String title) {
        String value = title.length() > ChatProperties.TITLE_COLUMN_LIMIT
                ? title.substring(0, ChatProperties.TITLE_COLUMN_LIMIT).trim() : title;
        return dsl.update(CHAT)
                .set(CHAT.TITLE, value)
                .where(CHAT.ID.eq(chatId).and(CHAT.TITLE.isNull()))
                .execute() == 1;
    }

    private static Condition condition(UUID userId, String query, KeysetCursor.Position cursor) {
        Condition condition = CHAT.USER_ID.eq(userId);
        if (query != null && !query.isBlank()) {
            condition = condition.and(CHAT.TITLE.isNotNull()).and(DSL.lower(CHAT.TITLE).like(buildLikePattern(query), '\\'));
        }
        if (cursor != null) {
            condition = condition.and(DSL.row(CHAT.UPDATED_AT, CHAT.ID).lt(cursor.getTime(), cursor.getId()));
        }
        return condition;
    }

    private ChatInternalState map(Record record) {
        return ChatInternalState.builder()
                .id(record.get(CHAT.ID))
                .userId(record.get(CHAT.USER_ID))
                .title(record.get(CHAT.TITLE))
                .createdAt(UtcTime.toUtc(record.get(CHAT.CREATED_AT)))
                .updatedAt(UtcTime.toUtc(record.get(CHAT.UPDATED_AT)))
                .build();
    }

    private static String buildLikePattern(String query) {
        String escaped = query.trim().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
