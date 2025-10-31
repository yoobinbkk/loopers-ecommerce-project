package com.loopers.interfaces.api.user;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "User API", description = "회원 관련 API 입니다.")
public interface UserApiSpec {

    @Operation(
            summary = "회원가입"
            , description = "회원 정보를 저장합니다."
    )
    ApiResponse<UserDto.UserResponse> saveUser(
            @Schema(name = "회원 정보", description = "회원 정보 request DTO")
            UserDto.CreateUserRequest createUserRequest
    );

    @Operation(
            summary = "회원 조회"
            , description = "로그인 ID 로 회원 조회합니다."
    )
    ApiResponse<UserDto.UserResponse> getUser(
            @Schema(name = "로그인 ID", description = "회원 조회할 로그인 ID")
            String loginId
    );

    @Operation(
            summary = "포인트 조회"
            , description = "회원의 포인트 조회합니다."
    )
    ApiResponse<Integer> getUserPoint(
            @Schema(name = "로그인 ID", description = "포인트 조회할 회원의 로그인 ID")
            String loginId,
            @Parameter(
                    name = "X-USER-ID"
                    , in = ParameterIn.HEADER
                    , description = "요청자 사용자 ID 헤더"
            )
            String xUserId
    );

    @Operation(
            summary = "포인트 충전"
            , description = "회원의 포인트 충전합니다."
    )
    ApiResponse<Integer> addUserPoint(
            @Schema(name = "로그인 ID", description = "포인트 충전할 회원의 로그인 ID")
            @Parameter(name = "loginId", in = ParameterIn.PATH)
            String loginId,
            @Schema(name = "충전할 포인트", description = "포인트 충전할 회원의 로그인 ID")
            @Parameter(name = "userPoint", in = ParameterIn.QUERY)
            Integer userPoint
    );
}
