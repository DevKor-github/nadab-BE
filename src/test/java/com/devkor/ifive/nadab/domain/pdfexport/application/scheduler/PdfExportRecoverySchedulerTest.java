package com.devkor.ifive.nadab.domain.pdfexport.application.scheduler;

import com.devkor.ifive.nadab.domain.pdfexport.application.PdfExportTxService;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportJob;
import com.devkor.ifive.nadab.domain.pdfexport.core.repository.PdfExportJobRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 환불 한 건이 실패해도 스윕이 나머지를 계속 회수하는지 검증한다.
 * 회수 결과(상태·에러코드·잔액)와 임계 경계는 PdfExportCrashRecoveryTest 가 실제 DB로 본다.
 */
class PdfExportRecoverySchedulerTest {

    private PdfExportJobRepository jobRepository;
    private PdfExportTxService txService;
    private PdfExportRecoveryScheduler scheduler;

    @BeforeEach
    void setUp() {
        jobRepository = mock(PdfExportJobRepository.class);
        txService = mock(PdfExportTxService.class);
        scheduler = new PdfExportRecoveryScheduler(jobRepository, txService);
    }

    @Test
    void 한_건이_실패해도_나머지는_회수한다() {
        // 목 생성을 when(...) 밖에서 끝낸다 — 스터빙 도중에 또 스터빙하면 Mockito가 거부한다.
        List<PdfExportJob> stuck =
                List.of(stuckJob(11L, 7L, 101L), stuckJob(12L, 8L, 102L), stuckJob(13L, 9L, 103L));
        when(jobRepository.findStuckInProgress(any(), anyInt())).thenReturn(stuck);
        doThrow(new IllegalStateException("wallet down"))
                .when(txService).failAndRefund(eq(8L), anyLong(), anyLong(), anyString());

        scheduler.recoverStuckJobs();

        // 실패한 건에서 멈추지 않고 세 건 모두 시도한다. 삼킨 건은 IN_PROGRESS로 남아 다음 주기가 다시 줍는다.
        verify(txService, times(3)).failAndRefund(anyLong(), anyLong(), anyLong(), anyString());
        verify(txService).failAndRefund(9L, 13L, 103L, "PDF_EXPORT_GENERATION_TIMEOUT");
    }

    /** 조회 결과 대역 — 스윕이 읽는 건 id·userId·crystalLogId 뿐이다. */
    private PdfExportJob stuckJob(long jobId, long userId, long crystalLogId) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);

        PdfExportJob job = mock(PdfExportJob.class);
        when(job.getId()).thenReturn(jobId);
        when(job.getUser()).thenReturn(user);
        when(job.getCrystalLogId()).thenReturn(crystalLogId);
        return job;
    }
}