package com.aichat.aichatdemo2.feature.user.mapper;

import com.aichat.aichatdemo2.feature.user.dto.state.AuthInternalState;
import com.aichat.aichatdemo2.feature.user.dto.state.UserInternalState;
import com.aichat.aichatdemo2.feature.user.dto.web.response.AuthResponse;
import com.aichat.aichatdemo2.feature.user.dto.web.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserAutoMapper {

    UserResponse mapToUser(UserInternalState state);

    @Mapping(target = "tokenType", constant = "Bearer")
    @Mapping(target = "user", source = "user")
    AuthResponse mapToAuth(AuthInternalState state);
}
