package com.devkor.ifive.nadab.domain.pdfexport.application.helper;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 유저별 다운로드 URL 발급 빈도 제한(in-memory fixed-window).
 * 맵 키가 userId라 크기 상한 = 유저 수, 만료 엔트리는 다음 발급 때 덮어써 리셋 → eviction sweep 없이 둔다.
 */
@Component
public class PdfExportDownloadRateLimiter {

    private static final int MAX_PER_WINDOW = 20;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final long WINDOW_NANOS = WINDOW.toNanos();

    private final ConcurrentHashMap<Long, Window> windows = new ConcurrentHashMap<>();

    /** compute()로 (만료 시 리셋 + 카운트 증가)를 키별 원자 연산 처리 → 스레드 안전. 초과면 false. */
    public boolean tryAcquire(Long userId) {
        long now = System.nanoTime();
        Window window = windows.compute(userId, (id, current) -> {
            if (current == null || now - current.startNanos >= WINDOW_NANOS) {
                return new Window(now);
            }
            current.count++;
            return current;
        });
        return window.count <= MAX_PER_WINDOW;
    }

    private static final class Window {
        private final long startNanos;
        private int count;

        private Window(long startNanos) {
            this.startNanos = startNanos;
            this.count = 1;
        }
    }
}