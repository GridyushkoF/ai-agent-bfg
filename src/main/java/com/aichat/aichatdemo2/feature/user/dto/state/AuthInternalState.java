package com.aichat.aichatdemo2.feature.user.dto.state;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthInternalState {

    @ToString.Exclude
    private String accessToken;
    private UserInternalState user;
}
