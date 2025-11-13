package com.loopers.application.user;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UserFacade {

    private final UserService userService;
    private final PointService pointService;

    /**
     * 회원가입 (User 저장하기)
     * @param userInfo
     * @return
     */
    @Transactional
    public UserInfo saveUser(UserInfo userInfo) {

        // 이미 존재하는 회원 객체가 있는지 확인
        Optional<User> foundUser = userService.findUserByLoginId(userInfo.loginId());
        if(foundUser.isPresent()){
            throw new CoreException(ErrorType.CONFLICT, "이미 존재하는 회원을 다시 저장 못합니다.");
        }

        // 이미 존재하는 회원 포인트 객체가 있는지 확인
        Optional<Point> foundPoint = pointService.findByUserLoginId(userInfo.loginId());
        if(foundPoint.isPresent()){
            throw new CoreException(ErrorType.CONFLICT, "이미 존재하는 회원의 포인트를 다시 저장 못합니다.");
        }

        // 포인트 객체 생성 (User 객체는 NULL)
        Point point = Point.builder()
                .user(null)
                .amount(BigDecimal.valueOf(0))
                .build();

        // 회원 객체 생성 (point 참조)
        User user = User.builder()
                .loginId(userInfo.loginId())
                .email(userInfo.email())
                .birthday(userInfo.birthday())
                .gender(userInfo.gender())
                .point(point)
                .build();

        // 회원 저장 (Cascade로 Point도 함께 저장됨)
        User savedUser = userService.saveUser(user)
                .orElseThrow(() -> new CoreException(ErrorType.INTERNAL_ERROR, "User 를 저장하지 못했습니다. (참조된 포인트 객체와 함께 저장)"));

        // Point의 user 참조 설정 (영속성 컨텍스트가 자동으로 UPDATE)
        point.setUser(savedUser);

        return UserInfo.from(savedUser);
    }

    /**
     * 회원 조회 (User 정보 가져오기)
     * @param loginId
     * @return
     */
    @Transactional(readOnly = true)
    public UserInfo getUser(String loginId){
        User user = userService.findUserByLoginId(loginId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "[loginId = " + loginId + "] User 를 찾지 못했습니다."));
        return UserInfo.from(user);
    }
}
