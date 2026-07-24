package com.devkor.ifive.nadab.domain.pdfexport.application.listener;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.EmotionCode;
import com.devkor.ifive.nadab.domain.pdfexport.application.PdfExportTxService;
import com.devkor.ifive.nadab.domain.pdfexport.application.event.PdfExportCompletedEvent;
import com.devkor.ifive.nadab.domain.pdfexport.application.render.PdfHtmlAssembler;
import com.devkor.ifive.nadab.domain.pdfexport.application.render.PdfRenderer;
import com.devkor.ifive.nadab.domain.pdfexport.core.dto.PdfAnswerRowDto;
import com.devkor.ifive.nadab.domain.pdfexport.core.dto.PdfExportRequestedEventDto;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportJob;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportType;
import com.devkor.ifive.nadab.domain.pdfexport.core.repository.PdfExportJobRepository;
import com.devkor.ifive.nadab.domain.pdfexport.core.repository.PdfExportQueryRepository;
import com.devkor.ifive.nadab.domain.pdfexport.infra.PdfExportStorage;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import org.junit.jupiter.api.AfterEach;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * PdfExportGenerationListener 오케스트레이션 검증 — 유형별 조회 분기·업로드·확정·완료 이벤트·실패 환불·사진 스킵의 "호출 순서"만.
 * 협력자(assembler·renderer)·DB·S3·과금 Tx는 전부 목(풀컨텍스트·DB 없이 돈다). 렌더 충실도는 PdfPreviewTest 담당.
 * 사진 리졸버(resolvePhoto)만은 assembler에 넘긴 Function 을 붙잡아 실제 동작(다운로드→PdfImage / 실패 스킵)까지 검증.
 */
class PdfExportGenerationListenerTest {

    private static final long JOB_ID = 1L;
    private static final long USER_ID = 7L;
    private static final long CRYSTAL_LOG_ID = 42L;
    private static final String RESULT_KEY = "local/pdf-exports/7/abcd-uuid.pdf";
    private static final LocalDate START = LocalDate.parse("2026-01-01");
    private static final LocalDate END = LocalDate.parse("2026-01-31");
    private static final String XHTML = "<html/>";
    private static final PdfExportRequestedEventDto EVENT =
            new PdfExportRequestedEventDto(JOB_ID, USER_ID, CRYSTAL_LOG_ID);

    private PdfExportJobRepository jobRepository;
    private PdfExportQueryRepository queryRepository;
    private PdfHtmlAssembler assembler;
    private PdfRenderer renderer;
    private PdfExportStorage storage;
    private PdfExportTxService txService;
    private ApplicationEventPublisher eventPublisher;
    private PdfExportGenerationListener listener;

    /** 렌더러가 반환하는 결과 임시파일(리스너가 업로드 후 삭제한다). */
    private Path pdfFile;

    @BeforeEach
    void setUp() throws IOException {
        jobRepository = mock(PdfExportJobRepository.class);
        queryRepository = mock(PdfExportQueryRepository.class);
        assembler = mock(PdfHtmlAssembler.class);
        renderer = mock(PdfRenderer.class);
        storage = mock(PdfExportStorage.class);
        txService = mock(PdfExportTxService.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        pdfFile = Files.createTempFile("test-pdf-export-", ".pdf");
        Files.write(pdfFile, "%PDF-1.4 rendered".getBytes());

        // 기본 해피패스 스텁(각 테스트에서 필요 시 덮어씀).
        when(assembler.assemble(any(), any(), any(), any(), any(), any()))
                .thenReturn(new PdfHtmlAssembler.AssembledDocument(XHTML, Map.of()));
        when(renderer.render(any(), any())).thenReturn(pdfFile);

        listener = new PdfExportGenerationListener(
                jobRepository, queryRepository, assembler, renderer, storage, txService, eventPublisher);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(pdfFile);
    }

    @Test
    void 답변만_답변만조회하고_렌더바이트를_업로드_확정_완료이벤트() {
        givenJob(PdfExportType.ANSWER_ONLY);
        List<PdfAnswerRowDto> answers = List.of(
                answer("2026-01-05", "오늘 가장 고마웠던 순간은?", null),
                answer("2026-01-12", "지금 마음의 온도는?", "answers/7/photo.webp"));
        when(queryRepository.findAnswersInPeriod(USER_ID, START, END)).thenReturn(answers);

        listener.handle(EVENT);

        // 답변 유형 = 답변만 조회, 리포트 조회 없음(리스트 빈 채 assemble).
        verify(assembler).assemble(eq(PdfExportType.ANSWER_ONLY), eq(answers),
                eq(List.of()), eq(List.of()), eq(List.of()), any());
        verify(queryRepository, never()).findWeeklyReportsInPeriod(anyLong(), any(), any());
        verify(queryRepository, never()).findMonthlyReportsInPeriod(anyLong(), any(), any());
        verify(queryRepository, never()).findMonthlyReportsV2InPeriod(anyLong(), any(), any());

        // job 결과 키로 업로드 + 파일명 2종(한글·ASCII 폴백) 각인.
        verify(storage).upload(RESULT_KEY, pdfFile,
                "나답_나에게답하다_20260101-20260131.pdf",
                "nadab_20260101-20260131.pdf");

        verify(txService).confirm(JOB_ID, CRYSTAL_LOG_ID);
        verify(txService, never()).failAndRefund(anyLong(), anyLong(), anyLong(), any());

        ArgumentCaptor<PdfExportCompletedEvent> completed = ArgumentCaptor.forClass(PdfExportCompletedEvent.class);
        verify(eventPublisher).publishEvent(completed.capture());
        assertThat(completed.getValue().getJobId()).isEqualTo(JOB_ID);
        assertThat(completed.getValue().getUserId()).isEqualTo(USER_ID);
    }

    @Test
    void 리포트만_주간과_월간V1V2를_조회하고_답변조회는안함() {
        givenJob(PdfExportType.REPORT_ONLY);
        when(queryRepository.findWeeklyReportsInPeriod(USER_ID, START, END)).thenReturn(List.of());
        when(queryRepository.findMonthlyReportsInPeriod(USER_ID, START, END)).thenReturn(List.of());
        when(queryRepository.findMonthlyReportsV2InPeriod(USER_ID, START, END)).thenReturn(List.of());

        listener.handle(EVENT);

        // 월간은 V1(레거시)·V2 둘 다 조회 — 2026-05 이전 월간 누락 방지.
        verify(queryRepository).findWeeklyReportsInPeriod(USER_ID, START, END);
        verify(queryRepository).findMonthlyReportsInPeriod(USER_ID, START, END);
        verify(queryRepository).findMonthlyReportsV2InPeriod(USER_ID, START, END);
        verify(queryRepository, never()).findAnswersInPeriod(anyLong(), any(), any());

        verify(storage).upload(eq(RESULT_KEY), eq(pdfFile), any(), any());
        verify(txService).confirm(JOB_ID, CRYSTAL_LOG_ID);
        verify(eventPublisher).publishEvent(any(PdfExportCompletedEvent.class));
    }

    @Test
    void 렌더결과_임시파일은_업로드후_삭제된다() {
        givenJob(PdfExportType.ANSWER_ONLY);
        when(queryRepository.findAnswersInPeriod(USER_ID, START, END)).thenReturn(List.of());
        assertThat(Files.exists(pdfFile)).isTrue();

        listener.handle(EVENT);

        // 업로드 성공 후 로컬 임시파일은 남기지 않는다(finally 정리).
        assertThat(Files.exists(pdfFile)).isFalse();
    }

    @Test
    void 리포트답변_답변과_리포트4종전부_조회() {
        givenJob(PdfExportType.REPORT_AND_ANSWER);
        when(queryRepository.findAnswersInPeriod(USER_ID, START, END)).thenReturn(List.of());
        when(queryRepository.findWeeklyReportsInPeriod(USER_ID, START, END)).thenReturn(List.of());
        when(queryRepository.findMonthlyReportsInPeriod(USER_ID, START, END)).thenReturn(List.of());
        when(queryRepository.findMonthlyReportsV2InPeriod(USER_ID, START, END)).thenReturn(List.of());

        listener.handle(EVENT);

        verify(queryRepository).findAnswersInPeriod(USER_ID, START, END);
        verify(queryRepository).findWeeklyReportsInPeriod(USER_ID, START, END);
        verify(queryRepository).findMonthlyReportsInPeriod(USER_ID, START, END);
        verify(queryRepository).findMonthlyReportsV2InPeriod(USER_ID, START, END);
        verify(txService).confirm(JOB_ID, CRYSTAL_LOG_ID);
    }

    @Test
    void 렌더실패시_확정없이_실패환불_안전한코드이름만() {
        givenJob(PdfExportType.ANSWER_ONLY);
        when(queryRepository.findAnswersInPeriod(USER_ID, START, END))
                .thenReturn(List.of(answer("2026-01-05", "질문", null)));
        when(renderer.render(any(), any())).thenThrow(new RuntimeException("openhtmltopdf boom"));

        listener.handle(EVENT);

        // 어디서 터지든: 확정 안 하고, 실패 확정 + 환불. error_code 엔 안전한 ErrorCode enum 이름만(메시지·스택 금지).
        verify(txService).failAndRefund(USER_ID, JOB_ID, CRYSTAL_LOG_ID, "PDF_EXPORT_GENERATION_FAILED");
        verify(txService, never()).confirm(anyLong(), anyLong());
        verify(storage, never()).upload(any(), any(), any(), any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @SuppressWarnings("unchecked")
    void 사진리졸버_정상은_jpeg바이트_실패는_스킵() throws Exception {
        givenJob(PdfExportType.ANSWER_ONLY);
        when(queryRepository.findAnswersInPeriod(USER_ID, START, END)).thenReturn(List.of());

        listener.handle(EVENT);

        // assembler 에 넘긴 사진 리졸버(this::resolvePhoto)를 붙잡아 실제 동작을 검증한다.
        ArgumentCaptor<Function<String, Optional<byte[]>>> resolverCaptor =
                ArgumentCaptor.forClass(Function.class);
        verify(assembler).assemble(any(), any(), any(), any(), any(), resolverCaptor.capture());
        Function<String, Optional<byte[]>> resolvePhoto = resolverCaptor.getValue();

        // 정상: S3 원본 → PdfImage 로 정사각 리샘플 → JPEG 바이트(asset: 서빙용).
        when(storage.download("answers/7/ok.webp")).thenReturn(syntheticPhotoBytes());
        Optional<byte[]> ok = resolvePhoto.apply("answers/7/ok.webp");
        assertThat(ok).isPresent();
        assertThat(ok.get()).hasSizeGreaterThan(1000);
        // JPEG SOI 매직 바이트(0xFF 0xD8) — 실제 JPEG 인코딩 결과임을 확인.
        assertThat(ok.get()[0] & 0xFF).isEqualTo(0xFF);
        assertThat(ok.get()[1] & 0xFF).isEqualTo(0xD8);

        // 실패: 개별 사진 다운로드 실패는 전체를 막지 않고 그 사진만 건너뛴다(Optional.empty).
        doThrow(new RuntimeException("object not found")).when(storage).download("answers/7/broken.webp");
        assertThat(resolvePhoto.apply("answers/7/broken.webp")).isEmpty();
    }

    /* ── helpers ── */

    private void givenJob(PdfExportType type) {
        PdfExportJob job = PdfExportJob.createPending(mock(User.class), type, START, END, RESULT_KEY);
        when(jobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));
    }

    private PdfAnswerRowDto answer(String date, String question, String imageKey) {
        return new PdfAnswerRowDto(LocalDate.parse(date), "본문 " + question, imageKey,
                question, InterestCode.EMOTION, EmotionCode.PEACE);
    }

    /** 파일 없이 코드로 만든 합성 사진 바이트(비정사각 800×600 → PdfImage 의 cover-crop 경로도 탄다). */
    private static byte[] syntheticPhotoBytes() throws Exception {
        BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setPaint(new GradientPaint(0, 0, new Color(0x5D57F6), 800, 600, new Color(0xB5E7FF)));
            g.fillRect(0, 0, 800, 600);
        } finally {
            g.dispose();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }
}