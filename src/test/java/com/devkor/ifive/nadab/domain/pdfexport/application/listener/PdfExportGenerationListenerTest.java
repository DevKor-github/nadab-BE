package com.devkor.ifive.nadab.domain.pdfexport.application.listener;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.EmotionCode;
import com.devkor.ifive.nadab.domain.pdfexport.application.PdfExportTxService;
import com.devkor.ifive.nadab.domain.pdfexport.application.event.PdfExportCompletedEvent;
import com.devkor.ifive.nadab.domain.pdfexport.application.event.PdfExportFailedEvent;
import com.devkor.ifive.nadab.domain.pdfexport.application.helper.PdfExportRenderQueue;
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
 * PdfExportGenerationListener 가 무엇을 부르고 무엇을 안 부르는지 검증한다.
 * 유형별 조회 분기 · 업로드 · 완료 확정 · 완료 이벤트 · 실패 시 환불 · 사진 스킵이 대상이다.
 * 협력자(assembler·renderer)·DB·S3·과금 트랜잭션은 전부 목이라 스프링도 DB도 없이 돈다. 렌더 결과물의 품질은 PdfPreviewTest 몫.
 * 사진만은 예외로, assembler 에 넘긴 조회 함수를 붙잡아 실제로 준비된 바이트(S3 원본 → JPEG / 실패는 빈 값)까지 확인한다.
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
    private PdfExportRenderQueue renderQueue;
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
        renderQueue = mock(PdfExportRenderQueue.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        pdfFile = Files.createTempFile("test-pdf-export-", ".pdf");
        Files.write(pdfFile, "%PDF-1.4 rendered".getBytes());

        // 기본 해피패스 스텁(각 테스트에서 필요 시 덮어씀).
        when(assembler.assemble(any(), any(), any(), any(), any(), any()))
                .thenReturn(new PdfHtmlAssembler.AssembledDocument(XHTML, Map.of()));
        when(renderer.render(any(), any())).thenReturn(pdfFile);
        when(txService.confirm(anyLong(), anyLong())).thenReturn(true);

        listener = new PdfExportGenerationListener(
                jobRepository, queryRepository, assembler, renderer, storage, txService, renderQueue, eventPublisher);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(pdfFile);
    }

    @Test
    void 답변만_유형은_답변만_조회해_업로드하고_확정한_뒤_완료를_알린다() throws Exception {
        givenJob(PdfExportType.ANSWER_ONLY);
        List<PdfAnswerRowDto> answers = List.of(
                answer("2026-01-05", "오늘 가장 고마웠던 순간은?", null),
                answer("2026-01-12", "지금 마음의 온도는?", "answers/7/photo.webp"));
        when(queryRepository.findAnswersInPeriod(USER_ID, START, END)).thenReturn(answers);
        when(storage.download("answers/7/photo.webp")).thenReturn(syntheticPhotoBytes());

        listener.handle(EVENT);

        // ANSWER_ONLY 는 답변만 조회한다. 리포트 3종은 조회 자체를 안 하고 빈 리스트로 assemble 에 넘어간다.
        verify(assembler).assemble(eq(PdfExportType.ANSWER_ONLY), eq(answers),
                eq(List.of()), eq(List.of()), eq(List.of()), any());
        verify(queryRepository, never()).findWeeklyReportsInPeriod(anyLong(), any(), any());
        verify(queryRepository, never()).findMonthlyReportsInPeriod(anyLong(), any(), any());
        verify(queryRepository, never()).findMonthlyReportsV2InPeriod(anyLong(), any(), any());

        // job 이 들고 있던 결과 키로 업로드하고, 파일명 2종(한글 + ASCII 폴백)을 S3 객체에 각인한다.
        verify(storage).upload(RESULT_KEY, pdfFile,
                "나답_20260101-20260131.pdf",
                "nadab_20260101-20260131.pdf");

        verify(txService).confirm(JOB_ID, CRYSTAL_LOG_ID);
        verify(txService, never()).failAndRefund(anyLong(), anyLong(), anyLong(), any());

        ArgumentCaptor<PdfExportCompletedEvent> completed = ArgumentCaptor.forClass(PdfExportCompletedEvent.class);
        verify(eventPublisher).publishEvent(completed.capture());
        assertThat(completed.getValue().getJobId()).isEqualTo(JOB_ID);
        assertThat(completed.getValue().getUserId()).isEqualTo(USER_ID);
    }

    @Test
    void 리포트만_유형은_주간과_월간_V1_V2를_조회하고_답변은_조회하지_않는다() {
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
    void 알림_발행이_실패해도_완료를_되돌리지_않는다() {
        givenJob(PdfExportType.ANSWER_ONLY);
        when(queryRepository.findAnswersInPeriod(USER_ID, START, END)).thenReturn(List.of());
        doThrow(new RuntimeException("알림 큐 포화"))
                .when(eventPublisher).publishEvent(any(PdfExportCompletedEvent.class));

        listener.handle(EVENT);

        // 알림이 터진 시점엔 업로드·확정이 이미 끝나 있다. 여기서 실패로 되돌리면 멀쩡히 완료된 작업이 실패로 기록된다.
        verify(storage).upload(eq(RESULT_KEY), eq(pdfFile), any(), any());
        verify(txService).confirm(JOB_ID, CRYSTAL_LOG_ID);
        verify(txService, never()).failAndRefund(anyLong(), anyLong(), anyLong(), any());
    }

    @Test
    void 복구_스윕이_먼저_실패처리했으면_완료를_알리지_않는다() {
        givenJob(PdfExportType.ANSWER_ONLY);
        when(queryRepository.findAnswersInPeriod(USER_ID, START, END)).thenReturn(List.of());
        // confirm 이 markCompleted 경합에서 짐 = 복구 스윕이 먼저 FAILED·환불 처리했다.
        when(txService.confirm(anyLong(), anyLong())).thenReturn(false);

        listener.handle(EVENT);

        // 업로드는 됐지만 확정에 실패했으므로 완성 푸시를 보내면 안 된다(눌러도 아카이브에 없고 크리스탈은 환불됨).
        verify(storage).upload(any(), any(), any(), any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void 리포트답변_유형은_답변과_리포트를_모두_조회한다() {
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
    void 렌더가_실패하면_확정하지_않고_안전한_코드_이름으로_환불한다() {
        givenJob(PdfExportType.ANSWER_ONLY);
        when(queryRepository.findAnswersInPeriod(USER_ID, START, END))
                .thenReturn(List.of(answer("2026-01-05", "질문", null)));
        when(renderer.render(any(), any())).thenThrow(new RuntimeException("openhtmltopdf boom"));
        when(txService.failAndRefund(anyLong(), anyLong(), anyLong(), any())).thenReturn(true);

        listener.handle(EVENT);

        // 렌더·업로드·확정 어디서 터지든 완료 확정은 안 하고 실패 처리 + 환불로 간다.
        // error_code 에는 ErrorCode enum 이름만 넣는다 — getStatus 로 클라에 그대로 나가는 값이라 예외 메시지·스택이 새면 안 된다.
        verify(txService).failAndRefund(USER_ID, JOB_ID, CRYSTAL_LOG_ID, "PDF_EXPORT_GENERATION_FAILED");
        verify(txService, never()).confirm(anyLong(), anyLong());
        verify(storage, never()).upload(any(), any(), any(), any());

        // 생성 화면을 벗어난 사용자는 이 알림이 없으면 실패를 알 방법이 없다(아카이브·/current 모두 FAILED 미노출).
        ArgumentCaptor<PdfExportFailedEvent> failed = ArgumentCaptor.forClass(PdfExportFailedEvent.class);
        verify(eventPublisher).publishEvent(failed.capture());
        assertThat(failed.getValue().getJobId()).isEqualTo(JOB_ID);
        assertThat(failed.getValue().getUserId()).isEqualTo(USER_ID);
    }

    @Test
    void 복구_스윕이_먼저_환불했으면_실패_알림을_보내지_않는다() {
        givenJob(PdfExportType.ANSWER_ONLY);
        when(queryRepository.findAnswersInPeriod(USER_ID, START, END))
                .thenReturn(List.of(answer("2026-01-05", "질문", null)));
        when(renderer.render(any(), any())).thenThrow(new RuntimeException("openhtmltopdf boom"));
        // false = markFailed 경합에서 짐. 스윕이 이미 실패 확정·환불했고 알림도 그쪽이 보냈다.
        when(txService.failAndRefund(anyLong(), anyLong(), anyLong(), any())).thenReturn(false);

        listener.handle(EVENT);

        // 여기서 또 발행하면 같은 작업에 실패 푸시가 두 번 간다.
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @SuppressWarnings("unchecked")
    void 사진은_정상이면_jpeg로_준비되고_실패한_한_장만_건너뛴다() throws Exception {
        givenJob(PdfExportType.ANSWER_ONLY);
        when(queryRepository.findAnswersInPeriod(USER_ID, START, END)).thenReturn(List.of(
                answer("2026-01-05", "오늘 가장 고마웠던 순간은?", "answers/7/ok.webp"),
                answer("2026-01-06", "지금 마음의 온도는?", "answers/7/broken.webp")));
        when(storage.download("answers/7/ok.webp")).thenReturn(syntheticPhotoBytes());
        // 개별 사진 다운로드 실패는 전체를 막지 않고 그 사진만 건너뛴다.
        doThrow(new RuntimeException("object not found")).when(storage).download("answers/7/broken.webp");

        listener.handle(EVENT);

        // 사진은 assemble 전에 미리 준비되고, assembler 는 준비된 맵을 조회만 한다.
        ArgumentCaptor<Function<String, Optional<byte[]>>> resolverCaptor =
                ArgumentCaptor.forClass(Function.class);
        verify(assembler).assemble(any(), any(), any(), any(), any(), resolverCaptor.capture());
        Function<String, Optional<byte[]>> photos = resolverCaptor.getValue();

        // 정상 사진: S3 원본을 PdfImage 가 640×640 정사각으로 리샘플해 JPEG 바이트로 만든다(asset: 로 서빙될 형태).
        Optional<byte[]> ok = photos.apply("answers/7/ok.webp");
        assertThat(ok).isPresent();
        assertThat(ok.get()).hasSizeGreaterThan(1000);
        // JPEG SOI 매직 바이트(0xFF 0xD8) — 실제 JPEG 인코딩 결과임을 확인.
        assertThat(ok.get()[0] & 0xFF).isEqualTo(0xFF);
        assertThat(ok.get()[1] & 0xFF).isEqualTo(0xD8);

        // 실패한 사진은 맵에 없어 그 자리만 생략된다(나머지는 정상 렌더·업로드·확정).
        assertThat(photos.apply("answers/7/broken.webp")).isEmpty();
        verify(txService).confirm(JOB_ID, CRYSTAL_LOG_ID);
    }

    @Test
    void 사진이_한_장도_준비되지_않으면_확정하지_않고_환불한다() {
        givenJob(PdfExportType.ANSWER_ONLY);
        when(queryRepository.findAnswersInPeriod(USER_ID, START, END)).thenReturn(List.of(
                answer("2026-01-05", "오늘 가장 고마웠던 순간은?", "answers/7/a.webp"),
                answer("2026-01-06", "지금 마음의 온도는?", "answers/7/b.webp")));
        // S3 장애·권한 사고면 전멸로 나타난다. 그대로 두면 사진 없는 PDF 를 만들고 과금까지 확정된다.
        doThrow(new RuntimeException("access denied")).when(storage).download(any());

        listener.handle(EVENT);

        verify(txService).failAndRefund(USER_ID, JOB_ID, CRYSTAL_LOG_ID, "PDF_EXPORT_GENERATION_FAILED");
        verify(txService, never()).confirm(anyLong(), anyLong());
        verify(storage, never()).upload(any(), any(), any(), any());
    }

    @Test
    void 사진이_원래_없는_기간은_전멸_판정에_걸리지_않는다() {
        givenJob(PdfExportType.ANSWER_ONLY);
        when(queryRepository.findAnswersInPeriod(USER_ID, START, END)).thenReturn(List.of(
                answer("2026-01-05", "오늘 가장 고마웠던 순간은?", null),
                answer("2026-01-06", "지금 마음의 온도는?", null)));

        listener.handle(EVENT);

        // 사진을 안 올린 유저의 export 는 정상이다. 여기서 실패시키면 멀쩡한 요청을 죽인다.
        verify(txService).confirm(JOB_ID, CRYSTAL_LOG_ID);
        verify(txService, never()).failAndRefund(anyLong(), anyLong(), anyLong(), any());
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

    /**
     * 파일 없이 코드로 만든 합성 사진 바이트(레포에 바이너리를 두지 않는다).
     * 크기는 실서비스가 받는 답변 사진과 같은 1280×1280 — PdfImage 가 640으로 줄일 때 서브샘플 디코드까지 탄다.
     */
    private static byte[] syntheticPhotoBytes() throws Exception {
        int side = 1280;
        BufferedImage img = new BufferedImage(side, side, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setPaint(new GradientPaint(0, 0, new Color(0x5D57F6), side, side, new Color(0xB5E7FF)));
            g.fillRect(0, 0, side, side);
        } finally {
            g.dispose();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }
}