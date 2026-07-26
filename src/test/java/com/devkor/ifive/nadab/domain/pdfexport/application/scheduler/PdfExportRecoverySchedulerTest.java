package com.devkor.ifive.nadab.domain.pdfexport.application.scheduler;

import com.devkor.ifive.nadab.domain.pdfexport.application.PdfExportTxService;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportJob;
import com.devkor.ifive.nadab.domain.pdfexport.core.repository.PdfExportJobRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 멈춘 작업 회수 검증.
 * 임계 시각 계산과 상태 판정은 조회 쿼리 몫이라 여기선 스윕이 그 결과로 무엇을 하는지만 본다.
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
    void 멈춘_작업이_없으면_아무것도_안_한다() {
        when(jobRepository.findStuckInProgress(any(), anyInt())).thenReturn(List.of());

        scheduler.recoverStuckJobs();

        verifyNoInteractions(txService);
    }

    @Test
    void 멈춘_작업은_렌더를_다시_돌리지_않고_타임아웃_코드로_환불한다() {
        // 목 생성을 when(...) 밖에서 끝낸다 — 스터빙 도중에 또 스터빙하면 Mockito가 거부한다.
        List<PdfExportJob> stuck = List.of(stuckJob(11L, 7L, 101L), stuckJob(12L, 8L, 102L));
        when(jobRepository.findStuckInProgress(any(), anyInt())).thenReturn(stuck);

        scheduler.recoverStuckJobs();

        // 재렌더 없이 환불만 부르는 것이 이 스윕의 계약.
        verify(txService).failAndRefund(7L, 11L, 101L, "PDF_EXPORT_GENERATION_TIMEOUT");
        verify(txService).failAndRefund(8L, 12L, 102L, "PDF_EXPORT_GENERATION_TIMEOUT");
    }

    @Test
    void 한_건이_실패해도_나머지는_회수한다() {
        List<PdfExportJob> stuck =
                List.of(stuckJob(11L, 7L, 101L), stuckJob(12L, 8L, 102L), stuckJob(13L, 9L, 103L));
        when(jobRepository.findStuckInProgress(any(), anyInt())).thenReturn(stuck);
        doThrow(new IllegalStateException("wallet down"))
                .when(txService).failAndRefund(eq(8L), anyLong(), anyLong(), anyString());

        scheduler.recoverStuckJobs();

        verify(txService, times(3)).failAndRefund(anyLong(), anyLong(), anyLong(), anyString());
        verify(txService).failAndRefund(9L, 13L, 103L, "PDF_EXPORT_GENERATION_TIMEOUT");
    }

    @Test
    void 조회_임계는_지금보다_과거다() {
        when(jobRepository.findStuckInProgress(any(), anyInt())).thenReturn(List.of());
        OffsetDateTime before = OffsetDateTime.now();

        scheduler.recoverStuckJobs();

        ArgumentCaptor<OffsetDateTime> threshold = ArgumentCaptor.forClass(OffsetDateTime.class);
        verify(jobRepository).findStuckInProgress(threshold.capture(), anyInt());

        // 임계가 미래로 새면 렌더 중인 작업까지 쓸어버린다.
        assertThat(threshold.getValue()).isBefore(before.minus(Duration.ofMinutes(30)));
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