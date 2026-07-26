package com.devkor.ifive.nadab.domain.pdfexport.application.helper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 접수 판정 검증. 실행기 큐를 직접 읽는 판정이라 목이 아니라 진짜 실행기를 채워서 확인한다.
 */
class PdfExportRenderQueueTest {

    /** 붙잡아둔 워커를 풀어주는 스위치 */
    private CountDownLatch release;
    private ThreadPoolTaskExecutor executor;
    private PdfExportRenderQueue renderQueue;

    @BeforeEach
    void setUp() {
        release = new CountDownLatch(1);
        executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(40);
        executor.initialize();
        renderQueue = new PdfExportRenderQueue(executor);
    }

    @AfterEach
    void tearDown() {
        release.countDown();
        executor.shutdown();
    }

    @Test
    void 아무것도_없으면_받는다() {
        assertThat(renderQueue.pending()).isZero();
        assertThat(renderQueue.canAccept()).isTrue();
    }

    @Test
    void 렌더_중인_작업도_밀려_있는_것으로_센다() throws Exception {
        occupyWorker();

        // 큐는 비었고 워커만 돌고 있는 상태. 안 세면 상한이 1만큼 헐거워진다.
        assertThat(renderQueue.pending()).isEqualTo(1);
    }

    @Test
    void 상한까지_차면_거부하고_자리가_나면_다시_받는다() throws Exception {
        occupyWorker();
        for (int i = 0; i < 19; i++) {
            executor.execute(() -> { });
        }

        assertThat(renderQueue.pending()).isEqualTo(20);
        assertThat(renderQueue.canAccept()).isFalse();

        release.countDown();
        waitUntilDrained();

        assertThat(renderQueue.canAccept()).isTrue();
    }

    @Test
    void 상한은_실행기_큐_용량보다_작다() throws Exception {
        occupyWorker();
        for (int i = 0; i < 19; i++) {
            executor.execute(() -> { });
        }

        // 실행기가 먼저 꽉 차면 차감된 작업이 버려진다. 우리가 막을 때까지 큐에 여유가 남아 있어야 함.
        assertThat(renderQueue.canAccept()).isFalse();
        assertThat(executor.getThreadPoolExecutor().getQueue().remainingCapacity()).isPositive();
    }

    /** 워커 1개를 latch로 붙잡아 이후 제출분이 큐에 쌓이게 함 */
    private void occupyWorker() throws InterruptedException {
        CountDownLatch started = new CountDownLatch(1);
        executor.execute(() -> {
            started.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        assertThat(started.await(5, TimeUnit.SECONDS)).isTrue();
    }

    private void waitUntilDrained() throws InterruptedException {
        for (int i = 0; i < 100 && renderQueue.pending() > 0; i++) {
            Thread.sleep(20);
        }
    }
}