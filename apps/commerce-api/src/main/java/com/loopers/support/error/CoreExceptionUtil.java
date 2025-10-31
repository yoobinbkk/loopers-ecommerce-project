package com.loopers.support.error;

import java.util.regex.Pattern;

public class CoreExceptionUtil {
    /**
     * 문자열이 NULL, Blank 이면 throw Bad Request CoreException
     */
    public static void validateNullOrBlank(String param, String message) {
        if (param == null || param.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, message);
        }
    }

    /**
     * 객체가 NULL 이면 throw Bad Request CoreException
     */
    public static void validateObjectNull(Object param, String message) {
        if (param == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, message);
        }
    }

    /**
     * 문자열이 pattern 에 맞지 않으면 throw Bad Request CoreException
     */
    public static void validatePattern(String param, String patternString, String message) {
        Pattern pattern = Pattern.compile(patternString);
        if(!pattern.matcher(param).matches()) {
            throw new CoreException(ErrorType.BAD_REQUEST, message);
        }
    }
}
