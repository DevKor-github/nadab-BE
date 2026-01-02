package com.devkor.ifive.nadab.global.security.handler;

import com.devkor.ifive.nadab.global.core.response.ApiErrorResponseDto;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT 인증 실패 시 401 Unauthorized 응답 처리
 * - 인증되지 않은 사용자가 보호된 리소스 접근 시
 * - 예: JWT 토큰이 없거나, 만료되었거나, 유효하지 않을 때
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Request attribute에서 ErrorCode 추출
        ErrorCode errorCode = (ErrorCode) request.getAttribute("errorCode");

        ApiErrorResponseDto<Void> body;
        if (errorCode != null) {
            // ErrorCode가 있으면 사용 (JwtAuthException에서 전달된 경우)
            body = ApiErrorResponseDto.error(
                    errorCode.getHttpStatus().value(),
                    errorCode.getCode(),
                    errorCode.getMessage()
            );
        } else {
            // ErrorCode가 없으면 기본 메시지
            body = ApiErrorResponseDto.error(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "AUTH_FAILED",
                    "JWT 인증에 실패했습니다."
            );
        }

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}