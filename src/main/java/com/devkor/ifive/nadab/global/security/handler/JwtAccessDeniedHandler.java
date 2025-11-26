package com.devkor.ifive.nadab.global.security.handler;

import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT 권한 없음 시 403 Forbidden 응답 처리
 * - 인증은 됐지만 접근 권한이 없을 때
 * - 예: USER 권한으로 ADMIN 리소스 접근 시도
 */
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiResponseDto<Void> body = ApiResponseDto.error(
                HttpServletResponse.SC_FORBIDDEN,
                "접근 권한이 없습니다."
        );

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}