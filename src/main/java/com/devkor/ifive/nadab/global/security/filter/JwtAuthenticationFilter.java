package com.devkor.ifive.nadab.global.security.filter;

import com.devkor.ifive.nadab.global.exception.JwtAuthException;
import com.devkor.ifive.nadab.global.security.handler.JwtAuthenticationEntryPoint;
import com.devkor.ifive.nadab.global.security.token.AccessTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 * - 요청 헤더에서 JWT 토큰 추출 및 검증
 * - 유효한 토큰이면 SecurityContext에 Authentication 설정
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AccessTokenProvider accessTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null) {
            try {
                Authentication authentication = accessTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("JWT 인증 성공 - URI: {}", request.getRequestURI());
            } catch (Exception e) {
                log.warn("JWT 인증 실패 - URI: {}, 원인: {}", request.getRequestURI(), e.getMessage());
                SecurityContextHolder.clearContext();

                // JwtAuthException이면 ErrorCode를 request attribute에 저장
                if (e instanceof JwtAuthException jwtAuthException) {
                    request.setAttribute("errorCode", jwtAuthException.getErrorCode());
                }

                jwtAuthenticationEntryPoint.commence(
                        request,
                        response,
                        new InsufficientAuthenticationException("JWT 인증 실패", e)
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    // HttpServletRequest에서 JWT 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}