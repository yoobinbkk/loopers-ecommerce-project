package com.loopers.support.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.loopers.support.error.CoreExceptionUtil.validateNullOrBlank;

@Component
public class AccessControlInterceptor implements HandlerInterceptor {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public boolean preHandle(
            HttpServletRequest request
            , HttpServletResponse response
            , Object handler
    ) throws Exception {

        // 포인트 조회 시 헤더에 X-USER-ID 가 없으면 BAD REQUEST
        String requestPath = request.getRequestURI().substring(request.getContextPath().length());
        if(pathMatcher.match("/api/user/*/getUserPoint", requestPath)) {
            String userIdHeader = request.getHeader("X-USER-ID");
            validateNullOrBlank(userIdHeader, "포인트 조회 시 헤더에 X-USER-ID 가 필요합니다.");
        }

        return true;
    }
}
