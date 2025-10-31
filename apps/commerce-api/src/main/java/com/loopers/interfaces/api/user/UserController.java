package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreExceptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.loopers.support.error.CoreExceptionUtil.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController implements UserApiSpec {

    private final UserFacade userFacade;

    @PostMapping("/saveUser")
    public ApiResponse<UserDto.UserResponse> saveUser(
            @RequestBody UserDto.CreateUserRequest cur
    ) {
        UserInfo userInfo = userFacade.saveUser(UserInfo.from(cur));
        UserDto.UserResponse userResponse = UserDto.UserResponse.from(userInfo);
        return ApiResponse.success(userResponse);
    }

    @GetMapping("/{loginId}")
    @Override
    public ApiResponse<UserDto.UserResponse> getUser(
        @PathVariable String loginId
    ) {
        UserInfo userInfo = userFacade.getUser(loginId);
        UserDto.UserResponse userResponse = UserDto.UserResponse.from(userInfo);
        return ApiResponse.success(userResponse);
    }

    @GetMapping("/{loginId}/getUserPoint")
    @Override
    public ApiResponse<Integer> getUserPoint(
            @PathVariable String loginId
            , @RequestHeader(value = "X-USER-ID", required = false) String xUserId
    ) {
        validateNullOrBlank(xUserId, "포인트 조회 시 헤더에 X-USER-ID 가 필요합니다.");
        Integer userPoint = userFacade.getUserPoint(loginId);
        return ApiResponse.success(userPoint);
    }

    @PostMapping("/{loginId}/addUserPoint")
    @Override
    public ApiResponse<Integer> addUserPoint(
            @PathVariable String loginId
            , @RequestParam Integer userPoint
    ) {
        Integer userTotalPoint = userFacade.addUserPoint(loginId, userPoint);
        return ApiResponse.success(userTotalPoint);
    }
}
