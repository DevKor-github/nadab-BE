package com.devkor.ifive.nadab.domain.auth.infra.oauth.state;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OAuth2 State 파라미터 관리 (CSRF 방지)
 * - In-Memory Map 기반
 * - TTL: 10분
 * - state는 정상적으로 소셜 로그인 시 대부분 삭제되므로 state가 일정 개수를 넘을 때만 만료된 state 정리
 */
@Component
public class StateManager {

    private static final long STATE_TTL_SECONDS = 600; // 10분
    private static final int CLEANUP_THRESHOLD = 100;  // state 정리 임계값
    private final Map<String, Instant> states = new ConcurrentHashMap<>();

    // State 생성 및 저장 (Map 크기가 임계값(100개) 초과 시에만 만료된 state 정리)
    public String generateAndStore() {
        String state = UUID.randomUUID().toString();
        states.put(state, Instant.now().plusSeconds(STATE_TTL_SECONDS));

        // 100개 초과하여 쌓였을 때만 정리
        if (states.size() > CLEANUP_THRESHOLD) {
            cleanExpired();
        }

        return state;
    }

    // State 검증 및 삭제 (한 번 검증하면 결과 상관없이 무조건 삭제)
    public boolean validateAndRemove(String state) {
        if (state == null || state.isBlank()) {
            return false;
        }

        Instant expiresAt = states.remove(state);
        if (expiresAt == null) {
            return false;
        }

        return Instant.now().isBefore(expiresAt);
    }

    // 만료된 state 정리
    private void cleanExpired() {
        Instant now = Instant.now();
        states.entrySet().removeIf(entry -> now.isAfter(entry.getValue()));
    }
}