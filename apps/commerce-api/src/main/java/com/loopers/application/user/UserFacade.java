package com.loopers.application.user;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UserFacade {
    private final UserService userService;

    @Transactional
    public UserInfo saveUser(UserInfo userInfo) {
        Optional<User> foundUser = userService.findUserById(userInfo.loginId());
        if(foundUser.isPresent()) {
            throw new CoreException(ErrorType.CONFLICT, "이미 존재하는 회원을 다시 저장 못합니다.");
        }

        User user = User.builder()
                .loginId(userInfo.loginId())
                .email(userInfo.email())
                .birthday(userInfo.birthday())
                .gender(userInfo.gender())
                .point(userInfo.point())
                .build();
        Optional<User> optSavedUser = userService.saveUser(user);
        return UserInfo.from(optSavedUser);
    }

    @Transactional(readOnly = true)
    public UserInfo getUser(String loginId){
        Optional<User> optUser = userService.findUserById(loginId);
        return UserInfo.from(optUser);
    }

    @Transactional(readOnly = true)
    public Integer getUserPoint(String loginId){
        User user = userService.findUserById(loginId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "[loginId = " + loginId + "] User를 찾을 수 없습니다."));
        return user.getPoint();
    }

    @Transactional
    public Integer addUserPoint(String loginId, Integer userPoint) {      User user = userService.findUserById(loginId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "[loginId = " + loginId + "] point를 충전할 User를 찾을 수 없습니다."));
        user.addPoint(userPoint);
        Optional<User> savedUser = userService.saveUser(user);
        return savedUser.get().getPoint();
    }
}
