package com.aichat.aichatdemo2.feature.user.repository;

import com.aichat.aichatdemo2.feature.user.dto.state.UserInternalState;
import com.aichat.aichatdemo2.platform.util.UtcTime;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static com.aichat.aichatdemo2.jooq.tables.AppUser.APP_USER;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final DSLContext dsl;

    public boolean existsByLogin(String login) {
        return dsl.fetchExists(dsl.selectOne().from(APP_USER).where(APP_USER.LOGIN.eq(login)));
    }

    public boolean existsByEmail(String email) {
        return dsl.fetchExists(dsl.selectOne().from(APP_USER).where(APP_USER.EMAIL.eq(email)));
    }

    public Optional<UserInternalState> findByLogin(String login) {
        return dsl.selectFrom(APP_USER).where(APP_USER.LOGIN.eq(login)).fetchOptional(this::map);
    }

    public Optional<UserInternalState> findById(UUID id) {
        return dsl.selectFrom(APP_USER).where(APP_USER.ID.eq(id)).fetchOptional(this::map);
    }

    public UserInternalState insert(String login, String email, String passwordHash) {
        UUID id = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        dsl.insertInto(APP_USER)
                .set(APP_USER.ID, id)
                .set(APP_USER.LOGIN, login)
                .set(APP_USER.EMAIL, email)
                .set(APP_USER.PASSWORD_HASH, passwordHash)
                .set(APP_USER.CREATED_AT, createdAt)
                .execute();
        return UserInternalState.builder()
                .id(id)
                .login(login)
                .email(email)
                .passwordHash(passwordHash)
                .createdAt(createdAt)
                .build();
    }

    private UserInternalState map(Record record) {
        return UserInternalState.builder()
                .id(record.get(APP_USER.ID))
                .login(record.get(APP_USER.LOGIN))
                .email(record.get(APP_USER.EMAIL))
                .passwordHash(record.get(APP_USER.PASSWORD_HASH))
                .createdAt(UtcTime.toUtc(record.get(APP_USER.CREATED_AT)))
                .build();
    }
}
