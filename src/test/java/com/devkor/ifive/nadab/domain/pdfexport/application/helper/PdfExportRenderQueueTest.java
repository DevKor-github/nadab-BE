package com.devkor.ifive.nadab.domain.pdfexport.application.helper;

import com.devkor.ifive.nadab.global.core.config.infra.AsyncConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 접수 판정 검증. 실행기 큐를 직접 읽는 판정이라 목이 아니라 진짜 실행기를 채워서 확인한다.
 * 실행기는 AsyncConfig 의 프로덕션 빈을 그대로 만들어 쓴다 — 상한과 큐 용량의 관계를 실제 설정값으로 봐야 한다.
 */
class PdfExportRenderQueueTest {

    /** 붙잡아둔 워커를 풀어주는 스위치 */
    private CountDownLatch release;
    private ThreadPoolTaskExecutor executor;
    private PdfExportRenderQueue renderQueue;

    @BeforeEach
    void setUp() {
        release = new CountDownLatch(1);
        executor = new AsyncConfig().pdfExportTaskExecutor();
        renderQueue = new PdfExportRenderQueue(executor);
    }

    @AfterEach
    void tearDown() {
        release.countDown();
        executor.shutdown();
    }

    @Test
    void 렌더_중인_작업도_밀려_있는_것으로_센다() throws Exception {
        occupyWorker();

        // 큐는 비었고 워커만 렌더 중인 상태. 렌더 중인 1건을 안 세면 상한 20이 21건까지 받아준다.
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
    void 상한에_도달해도_실행기_큐에는_자리가_남는다() throws Exception {
        occupyWorker();
        for (int i = 0; i < 19; i++) {
            executor.execute(() -> { });
        }

        // 우리 상한 20에서 막는 지금 실행기 큐 40엔 아직 자리가 있어야 한다. 반대로 실행기가 먼저 차면 AbortPolicy가 크리스탈 빠진 작업을 버린다.
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