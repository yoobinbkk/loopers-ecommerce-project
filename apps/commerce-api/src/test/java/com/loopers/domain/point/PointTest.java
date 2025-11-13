package com.loopers.domain.point;

import com.loopers.domain.user.Gender;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Point 테스트")
public class PointTest {

    @DisplayName("point 충전 테스트")
    @Nested
    class AddPointTest {

        final String validLoginId = "bobby34";
        final String validEmail = "bobby34@naver.com";
        final String validBirthday = "1994-04-08";
        final Gender validGender = Gender.MALE;
        final BigDecimal validPoint = BigDecimal.valueOf(0);

        @DisplayName("실패 케이스 : 0 이하의 정수로 포인트를 충전 시 실패")
        @Test
        void charge_inputZero_BadRequest() {
            // arrange
            Point point = Point.builder()
                    .user(null)
                    .amount(validPoint)
                    .build();

            BigDecimal requestPoint = BigDecimal.valueOf(0);

            // act
            CoreException result = assertThrows(CoreException.class,
                    () -> point.charge(requestPoint)
            );

            // assert
            assertEquals(ErrorType.BAD_REQUEST, result.getErrorType());
            assertEquals("충전할 point는 0 이하가 될 수 없습니다.", result.getCustomMessage());
        }

        @DisplayName("실패 케이스 : 음수로 포인트를 충전 시 실패")
        @Test
        void charge_inputBelowZero_BadRequest() {
            // arrange
            Point point = Point.builder()
                    .user(null)
                    .amount(validPoint)
                    .build();

            BigDecimal requestPoint = BigDecimal.valueOf(-10);

            // act
            CoreException result = assertThrows(CoreException.class,
                    () -> point.charge(requestPoint)
            );

            // assert
            assertEquals(ErrorType.BAD_REQUEST, result.getErrorType());
            assertEquals("충전할 point는 0 이하가 될 수 없습니다.", result.getCustomMessage());
        }
    }
}
