package com.devkor.ifive.nadab.global.security.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.*;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.devkor.ifive.nadab.global.core.properties.TokenProperties;
import com.devkor.ifive.nadab.global.exception.JwtAuthException;
import com.devkor.ifive.nadab.global.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Access Token 생성 및 검증
 * - JWT 방식 사용
 * - HMAC256 알고리즘(대칭키 기반)
 */
@Component
@RequiredArgsConstructor
public class AccessTokenProvider {

    private final TokenProperties tokenProperties;

    // Access Token(JWT) 생성
    public String generateToken(Long userId, List<String> userRoles) {
        Algorithm algorithm = Algorithm.HMAC256(tokenProperties.getJwtSecret());
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + tokenProperties.getAccessTokenExpiration());

        return JWT.create()
                .withSubject(userId.toString())
                .withClaim("roles", userRoles)
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .sign(algorithm);
    }

    // Access Token(JWT)에서 Authentication 객체 생성
    public Authentication getAuthentication(String token) {
        DecodedJWT decodedJWT = verifyAndDecode(token);

        Long userId = extractUserId(decodedJWT);
        List<String> roles = extractRoles(decodedJWT);

        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        UserPrincipal userPrincipal = new UserPrincipal(userId);
        return new UsernamePasswordAuthenticationToken(userPrincipal, null, authorities);
    }

    // Access Token(JWT) 검증 및 디코딩
    private DecodedJWT verifyAndDecode(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(tokenProperties.getJwtSecret());
            return JWT.require(algorithm)
                    .build()
                    .verify(token);
        } catch (TokenExpiredException e) {
            throw new JwtAuthException("토큰이 만료되었습니다.");
        } catch (SignatureVerificationException e) {
            throw new JwtAuthException("토큰 서명 검증에 실패했습니다.");
        } catch (JWTDecodeException e) {
            throw new JwtAuthException("토큰 형식이 올바르지 않습니다.");
        } catch (JWTVerificationException e) {
            throw new JwtAuthException("토큰 검증에 실패했습니다.");
        }
    }

    // Access Token(JWT)에서 userId 추출
    private Long extractUserId(DecodedJWT decodedJWT) {
        String subject = decodedJWT.getSubject();
        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException e) {
            throw new JwtAuthException("토큰의 유저 ID 형식이 올바르지 않습니다.");
        }
    }

    // Access Token(JWT)에서 roles 추출
    private List<String> extractRoles(DecodedJWT decodedJWT) {
        List<String> roles = decodedJWT.getClaim("roles").asList(String.class);
        if (roles == null) {
            throw new JwtAuthException("토큰에 권한 정보가 없습니다.");
        }
        return roles;
    }
}