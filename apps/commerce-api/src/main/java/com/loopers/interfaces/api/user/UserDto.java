package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserInfo;
import lombok.Builder;

public class UserDto {

    @Builder
    public record CreateUserRequest(
            String loginId
            , String email
            , String birthday
            , String gender
            , Integer point
    ) {}

    @Builder
    public record UserResponse(
            String loginId
            , String email
            , String birthday
            , String gender
            , Integer point
    ) {
        public static UserResponse from(UserInfo userInfo) {
            return UserResponse.builder()
                    .loginId(userInfo.loginId())
                    .email(userInfo.email())
                    .birthday(userInfo.birthday())
                    .gender(userInfo.gender())
                    .point(userInfo.point())
                    .build();
        }
    }

}
