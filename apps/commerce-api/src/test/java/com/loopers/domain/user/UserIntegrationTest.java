package com.loopers.domain.user;

import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserInfo;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User 통합 테스트")
@SpringBootTest
public class UserIntegrationTest {

    @Autowired
    private UserFacade userFacade;

    @MockitoSpyBean
    private UserRepository userRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {databaseCleanUp.truncateAllTables();}

    final String validLoginId = "bobby34";
    final String validEmail = "bobby34@naver.com";
    final String validBirthday = "1994-04-08";
    final String validGender = "M";
    final Integer validPoint = 0;

    @DisplayName("User 엔티티 생성")
    @Nested
    class CreateUserTest {

        @DisplayName("성공 케이스 : User 저장에 성공하는지 확인")
        @Test
        void saveUser_withValidFields_Success() {
            // arrange
            UserInfo userinfo = UserInfo.builder()
                    .loginId(validLoginId)
                    .email(validEmail)
                    .birthday(validBirthday)
                    .gender(validGender)
                    .point(validPoint)
                    .build();

            // act
            userFacade.saveUser(userinfo);

            // assert
            Mockito.verify(userRepository, Mockito.times(1))
                .save(Mockito.argThat(user -> 
                    user.getLoginId().equals(validLoginId)
                    && user.getEmail().equals(validEmail)
                    && user.getBirthday().equals(validBirthday)
                    && user.getGender().equals(validGender)
                    && user.getPoint().equals(validPoint)
                )
            );
        }

        @DisplayName("실패 케이스 : 이미 저장된 User 를 다시 저장하면 실패")
        @Test
        void saveUser_spyUserRepository_Success() {
            // arrange
            UserInfo userinfo = UserInfo.builder()
                    .loginId(validLoginId)
                    .email(validEmail)
                    .birthday(validBirthday)
                    .gender(validGender)
                    .point(validPoint)
                    .build();
            userFacade.saveUser(userinfo);

            // act
            CoreException result = assertThrows(CoreException.class,
                () -> userFacade.saveUser(userinfo));

            // assert
            assertEquals(ErrorType.CONFLICT, result.getErrorType());
        }

    }

    @DisplayName("User 엔티티 조회")
    @Nested
    class GetUserTest {

        @DisplayName("성공 케이스 : ID 의 User가 존재할 경우, 회원 정보 반환")
        @Test
        void getUser_idExists_Success() {
            // arrange
            UserInfo userinfo = UserInfo.builder()
                    .loginId(validLoginId)
                    .email(validEmail)
                    .birthday(validBirthday)
                    .gender(validGender)
                    .point(validPoint)
                    .build();
            userFacade.saveUser(userinfo);

            // act
            UserInfo userInfo = userFacade.getUser(validLoginId);

            // assert
            assertNotNull(userInfo);
            assertAll(
                    () -> assertEquals(userInfo.loginId(), validLoginId)
                    , () -> assertEquals(userInfo.email(), validEmail)
                    , () -> assertEquals(userInfo.birthday(), validBirthday)
                    , () -> assertEquals(userInfo.gender(), validGender)
                    , () -> assertEquals(userInfo.point(), validPoint)
            );
        }

        @DisplayName("실패 케이스 : ID 의 User가 존재하지 않을 경우, Null 반환")
        @Test
        void getUser_idNotExists_getNull() {
            // arrange

            // act
            UserInfo userInfo = userFacade.getUser(validLoginId);

            // assert
            assertNull(userInfo);
        }
    }

    @DisplayName("point 충전 테스트")
    @Nested
    class PointTest {

        @DisplayName("실패 케이스 : 존재하지 않는 User ID 로 충전을 시도한 경우, 실패")
        @Test
        void addPoint_withNoUser_NotFound() {
            // arrange
            Integer requestPoint = 1000;

            // act
            CoreException result = assertThrows(CoreException.class,
                    () -> userFacade.addUserPoint(validLoginId,  requestPoint)
            );

            // assert
            assertEquals(ErrorType.NOT_FOUND, result.getErrorType());
            assertTrue(result.getCustomMessage().endsWith("point를 충전할 User를 찾을 수 없습니다."));
        }
    }
}
