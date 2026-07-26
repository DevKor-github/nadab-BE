package com.devkor.ifive.nadab.domain.pdfexport.application.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 다운로드 URL 발급 빈도 제한 검증.
 */
class PdfExportDownloadRateLimiterTest {

    private static final long USER_ID = 7L;
    private static final int LIMIT = 20;

    private PdfExportDownloadRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new PdfExportDownloadRateLimiter();
    }

    @Test
    void 상한까지는_허용하고_넘으면_거부한다() {
        IntStream.range(0, LIMIT)
                .forEach(i -> assertThat(rateLimiter.tryAcquire(USER_ID)).isTrue());

        assertThat(rateLimiter.tryAcquire(USER_ID)).isFalse();
    }

    @Test
    void 한_유저가_상한을_채워도_다른_유저는_영향받지_않는다() {
        IntStream.range(0, LIMIT + 5).forEach(i -> rateLimiter.tryAcquire(USER_ID));

        // 카운터가 섞이면 한 명이 전체 발급을 막을 수 있다.
        assertThat(rateLimiter.tryAcquire(99L)).isTrue();
    }

    @Test
    void 거부된_뒤에도_계속_거부한다() {
        IntStream.range(0, LIMIT).forEach(i -> rateLimiter.tryAcquire(USER_ID));

        assertThat(rateLimiter.tryAcquire(USER_ID)).isFalse();
        assertThat(rateLimiter.tryAcquire(USER_ID)).isFalse();
    }
}