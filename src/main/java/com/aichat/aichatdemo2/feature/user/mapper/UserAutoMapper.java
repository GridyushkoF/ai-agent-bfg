package com.aichat.aichatdemo2.feature.user.mapper;

import com.aichat.aichatdemo2.feature.user.dto.AuthResponse;
import com.aichat.aichatdemo2.feature.user.dto.UserResponse;
import com.aichat.aichatdemo2.feature.user.state.AuthInternalState;
import com.aichat.aichatdemo2.feature.user.state.UserInternalState;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserAutoMapper {

    UserResponse toUser(UserInternalState state);

    @Mapping(target = "tokenType", constant = "Bearer")
    @Mapping(target = "user", source = "user")
    AuthResponse toAuth(AuthInternalState state);
}
