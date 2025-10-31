package com.loopers.application.user;

import com.loopers.domain.user.User;
import com.loopers.interfaces.api.user.UserDto;
import lombok.Builder;

import java.util.Optional;

@Builder
public record UserInfo(
        String loginId
        , String email
        , String birthday
        , String gender
        , Integer point
) {
    public static UserInfo from(Optional<User> optUser) {
        if(optUser.isPresent()) {
            User user = optUser.get();
            return UserInfo.builder()
                    .loginId(user.getLoginId())
                    .email(user.getEmail())
                    .birthday(user.getBirthday())
                    .gender(user.getGender())
                    .point(user.getPoint())
                    .build();
        }

        return null;
    }

    public static UserInfo from(UserDto.CreateUserRequest cur) {
        return UserInfo.builder()
                .loginId(cur.loginId())
                .email(cur.email())
                .birthday(cur.birthday())
                .gender(cur.gender())
                .point(cur.point())
                .build();
    }

}
