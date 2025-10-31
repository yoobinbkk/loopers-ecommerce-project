package com.loopers.domain.user;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.loopers.support.error.CoreExceptionUtil.*;

@Entity
@Table(name = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder(toBuilder = true)
public class User extends BaseEntity {

    private String loginId;
    private String email;
    private String birthday;
    private String gender;
    private Integer point;

    private User(
            String loginId
            , String email
            , String birthday
            , String gender
            , Integer point
    ) {
        this.loginId = loginId;
        this.email = email;
        this.birthday = birthday;
        this.gender = gender;
        this.point = point;
        this.guard();
    }

    @Override
    protected void guard() {
        // id : 영문, 숫자가 각각 최소 한 개씩 포함된 10자 이내 문자열 검사
        validateNullOrBlank(loginId, "ID가 비어있을 수 없습니다.");
        validatePattern(loginId, "^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]{1,10}$", "ID에 영문과 숫자만 10자 이내로 꼭 포함되어 있어야 합니다.");

        // email : `xx@yy.zz` 형식에 맞는지 검사
        validateNullOrBlank(email, "email이 비어있을 수 없습니다.");
        validatePattern(email, "^[a-zA-Z0-9]+@[a-zA-Z0-9]+\\.[a-zA-Z]{2,6}$", "email이 `xx@yy.zz` 형식에 맞아야 합니다.");

        // birthday : `yyyy-MM-dd` 형식에 맞는지 검사
        validateNullOrBlank(birthday, "birthday가 비어있을 수 없습니다.");
        validatePattern(birthday, "^\\d{4}-\\d{2}-\\d{2}$", "birthday가 `yyyy-MM-dd` 형식에 맞아야 합니다.");

        // gender : `M` or `F` 형식에 맞는지 검사
        validateNullOrBlank(gender, "gender가 비어있을 수 없습니다.");
        validatePattern(gender, "^[FM]{1}$", "gender가 `M` or `F` 형식에 맞아야 합니다.");

        // point : Null 이 되면 안 됩니다.
        validateObjectNull(point, "point가 Null 이 되면 안 됩니다.");
        if(point < 0) throw new CoreException(ErrorType.BAD_REQUEST, "point는 음수가 될 수 없습니다.");
    }

    public Integer addPoint(Integer point) {
        // 충전하는 포인트가 음수이면 BAD REQUEST CoreException 을 발생
        if(point <= 0) throw new CoreException(ErrorType.BAD_REQUEST, "충전할 point는 0 이하가 될 수 없습니다.");
        this.point += point;
        return this.point;
    }

}
