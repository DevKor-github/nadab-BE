package com.devkor.ifive.nadab.domain.pdfexport.application.helper;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 렌더 대기 줄 길이를 실행기 큐에서 직접 읽어 새 생성 요청을 받을지 판정.
 * MAX_PENDING은 AsyncConfig의 queueCapacity보다 작아야 함. 실행기가 거부하면 차감된 작업이 렌더도 못 하고 버려진다.
 */
@Component
public class PdfExportRenderQueue {

    /** 렌더 중 1건 포함, 동시에 밀려 있어도 되는 최대 작업 수 */
    private static final int MAX_PENDING = 20;

    private final ThreadPoolTaskExecutor executor;

    /** 한정자 없으면 @Primary 기본 풀이 주입됨 = 렌더가 도는 큐와 다른 큐를 읽게 됨 */
    public PdfExportRenderQueue(@Qualifier("pdfExportTaskExecutor") ThreadPoolTaskExecutor executor) {
        this.executor = executor;
    }

    /** 판정~제출 사이가 원자적이지 않아 동시 요청 수만큼 잠깐 초과 가능. 그 몫은 큐 여유가 받아줌 */
    public boolean canAccept() {
        return pending() < MAX_PENDING;
    }

    /** 대기 중 + 렌더 중 */
    public int pending() {
        ThreadPoolExecutor pool = executor.getThreadPoolExecutor();
        return pool.getQueue().size() + pool.getActiveCount();
    }
}