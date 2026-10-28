package com.aichat.aichatdemo2.feature.user.state;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInternalState {

    private UUID id;
    private String login;
    @ToString.Exclude
    private String email;
    @ToString.Exclude
    private String passwordHash;
    private OffsetDateTime createdAt;
}
