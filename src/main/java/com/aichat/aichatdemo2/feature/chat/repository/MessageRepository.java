package com.aichat.aichatdemo2.feature.chat.repository;

import com.aichat.aichatdemo2.feature.chat.dto.enumerated.MessageRole;
import com.aichat.aichatdemo2.feature.chat.dto.state.MessageInternalState;
import com.aichat.aichatdemo2.feature.chat.dto.state.MessagePageInternalState;
import com.aichat.aichatdemo2.platform.util.UtcTime;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.aichat.aichatdemo2.jooq.tables.Message.MESSAGE;

@Repository
@RequiredArgsConstructor
public class MessageRepository {

    private final DSLContext dsl;

    public MessageInternalState insert(UUID chatId, MessageRole role, String content, OffsetDateTime createdAt) {
        UUID id = UUID.randomUUID();
        dsl.insertInto(MESSAGE)
                .set(MESSAGE.ID, id)
                .set(MESSAGE.CHAT_ID, chatId)
                .set(MESSAGE.ROLE, role.name())
                .set(MESSAGE.CONTENT, content)
                .set(MESSAGE.CREATED_AT, createdAt)
                .execute();
        return MessageInternalState.builder()
                .id(id)
                .chatId(chatId)
                .role(role)
                .content(content)
                .createdAt(createdAt)
                .build();
    }

    public void delete(UUID messageId) {
        dsl.deleteFrom(MESSAGE).where(MESSAGE.ID.eq(messageId)).execute();
    }

    public MessagePageInternalState findPage(UUID chatId, int limit, KeysetCursor.Position cursor) {
        Condition condition = MESSAGE.CHAT_ID.eq(chatId);
        if (cursor != null) {
            condition = condition.and(DSL.row(MESSAGE.CREATED_AT, MESSAGE.ID).lt(cursor.getTime(), cursor.getId()));
        }
        List<MessageInternalState> fetched = dsl.selectFrom(MESSAGE)
                .where(condition)
                .orderBy(MESSAGE.CREATED_AT.desc(), MESSAGE.ID.desc())
                .limit(limit + 1)
                .fetch(this::map);
        PageSlice<MessageInternalState> slice = PageSlice.take(
                fetched, limit, message -> KeysetCursor.encode(message.getCreatedAt(), message.getId()));
        List<MessageInternalState> chronological = new ArrayList<>(slice.getItems());
        Collections.reverse(chronological);
        return MessagePageInternalState.builder().items(chronological).nextCursor(slice.getNextCursor()).build();
    }

    public List<MessageInternalState> findLatest(UUID chatId, int limit) {
        List<MessageInternalState> latest = new ArrayList<>(dsl.selectFrom(MESSAGE)
                .where(MESSAGE.CHAT_ID.eq(chatId))
                .orderBy(MESSAGE.CREATED_AT.desc(), MESSAGE.ID.desc())
                .limit(limit)
                .fetch(this::map));
        Collections.reverse(latest);
        return latest;
    }

    private MessageInternalState map(Record record) {
        return MessageInternalState.builder()
                .id(record.get(MESSAGE.ID))
                .chatId(record.get(MESSAGE.CHAT_ID))
                .role(MessageRole.valueOf(record.get(MESSAGE.ROLE)))
                .content(record.get(MESSAGE.CONTENT))
                .createdAt(UtcTime.toUtc(record.get(MESSAGE.CREATED_AT)))
                .build();
    }
}
