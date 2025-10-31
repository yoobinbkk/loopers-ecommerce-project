package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    ) {
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
