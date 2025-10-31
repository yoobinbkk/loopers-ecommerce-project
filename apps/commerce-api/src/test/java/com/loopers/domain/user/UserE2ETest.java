package com.loopers.domain.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserInfo;
import com.loopers.interfaces.api.user.UserDto;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("User E2E 테스트")
@SpringBootTest
@AutoConfigureMockMvc
public class UserE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private UserFacade userFacade;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    void tearDown() {databaseCleanUp.truncateAllTables();}

    final String validLoginId = "bobby34";
    final String validEmail = "bobby34@naver.com";
    final String validBirthday = "1994-04-08";
    final String validGender = "M";
    final int validPoint = 0;

    @DisplayName("User 엔티티 생성")
    @Nested
    class CreateUserTest {

        @DisplayName("성공 케이스 : 회원 가입이 성공해서 생성된 유저 정보를 응답으로 반환")
        @Test
        void saveUser_returnUserResponse_Success() throws Exception {
            // arrange
            UserDto.CreateUserRequest createUserRequest = UserDto.CreateUserRequest.builder()
                    .loginId(validLoginId)
                    .email(validEmail)
                    .birthday(validBirthday)
                    .gender(validGender)
                    .point(validPoint)
                    .build();
            String userJson = objectMapper.writeValueAsString(createUserRequest);

            // act
            mockMvc.perform(post("/api/user/saveUser")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(userJson))

            // assert
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.data.loginId").value(validLoginId))
                    .andExpect(jsonPath("$.data.email").value(validEmail))
                    .andExpect(jsonPath("$.data.birthday").value(validBirthday))
                    .andExpect(jsonPath("$.data.gender").value(validGender))
                    .andExpect(jsonPath("$.data.point").value(validPoint));
        }

        @DisplayName("실패 케이스 : 회원 가입 시 성별 없을 경우 `400 Bad Request` 응답 반환")
        @Test
        void saveUser_noGender_BadRequest() throws Exception {
            // arrange
            UserDto.CreateUserRequest createUserRequest = UserDto.CreateUserRequest.builder()
                    .loginId(validLoginId)
                    .email(validEmail)
                    .birthday(validBirthday)
                    .point(validPoint)
                    .build();
            String userJson = objectMapper.writeValueAsString(createUserRequest);

            // act
            mockMvc.perform(post("/api/user/saveUser")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(userJson))

                    // assert
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.meta.result").value("FAIL"))
                    .andExpect(jsonPath("$.meta.errorCode").value("Bad Request"))
                    .andExpect(jsonPath("$.meta.message").value("gender가 비어있을 수 없습니다."));
        }
    }

    @DisplayName("User 포인트 조회")
    @Nested
    class GetUserTest {

        @DisplayName("성공 케이스 : 포인트 조회에 성공할 경우, 보유 포인트를 응답으로 반환")
        @Test
        void getUserPoint_withValidFields_Success() throws Exception {
            // arrange
            UserDto.CreateUserRequest cur = UserDto.CreateUserRequest.builder()
                    .loginId(validLoginId)
                    .email(validEmail)
                    .birthday(validBirthday)
                    .gender(validGender)
                    .point(validPoint)
                    .build();

            userFacade.saveUser(UserInfo.from(cur));

            // act
            mockMvc.perform(get("/api/user/{loginId}/getUserPoint", validLoginId)
                            .header("X-USER-ID",  validLoginId)
                            .contentType(MediaType.APPLICATION_JSON))

            // assert
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isNumber())
                    .andExpect(jsonPath("$.data").value(validPoint));
        }

        @DisplayName("실패 케이스 : `X-USER-ID` 헤더가 없을 경우, `400 Bad Request` 응답을 반환")
        @Test
        void getUserPoint_XUserIdHeaderNotExists_BadRequest() throws Exception {
            // arrange

            // act
            mockMvc.perform(get("/api/user/{loginId}/getUserPoint", validLoginId)
                            .contentType(MediaType.APPLICATION_JSON))

            // assert
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.meta.message").value("포인트 조회 시 헤더에 X-USER-ID 가 필요합니다."));
        }
    }

    @DisplayName("User 포인트 충전")
    @Nested
    class AddUserTest {

        @DisplayName("성공 케이스 : 존재하는 User가 1000원을 충전할 경우, 충전된 보유 총량을 응답으로 반환")
        @Test
        void addUserPoint_add1000_getTotalPoints() throws Exception {
            // arrange
            Integer requestPoint = 1000;

            UserDto.CreateUserRequest cur = UserDto.CreateUserRequest.builder()
                    .loginId(validLoginId)
                    .email(validEmail)
                    .birthday(validBirthday)
                    .gender(validGender)
                    .point(validPoint)
                    .build();

            userFacade.saveUser(UserInfo.from(cur));

            // act
            mockMvc.perform(post("/api/user/{loginId}/addUserPoint", validLoginId)
                            .param("userPoint", requestPoint.toString())
                            .contentType(MediaType.APPLICATION_JSON))

                    // assert
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isNumber())
                    .andExpect(jsonPath("$.data").value(requestPoint));
        }

        @DisplayName("실패 케이스 : 존재하지 않는 User로 요청할 경우, `404 Not Found` 응답을 반환")
        @Test
        void addPoint_withNoUser_NotFound() throws Exception {
            // arrange
            Integer requestPoint = 1000;

            // act
            mockMvc.perform(post("/api/user/{loginId}/addUserPoint", validLoginId)
                            .param("userPoint", requestPoint.toString())
                            .contentType(MediaType.APPLICATION_JSON))

                    // assert
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.meta.message").value(endsWith("point를 충전할 User를 찾을 수 없습니다.")));
        }
    }
}
