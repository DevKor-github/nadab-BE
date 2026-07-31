package com.devkor.ifive.nadab.domain.pdfexport.application.listener;

import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReport;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2;
import com.devkor.ifive.nadab.domain.pdfexport.application.helper.PdfExportFileNames;
import com.devkor.ifive.nadab.domain.pdfexport.application.helper.PdfExportRenderQueue;
import com.devkor.ifive.nadab.domain.pdfexport.application.helper.PdfPhotoPrefetcher;
import com.devkor.ifive.nadab.domain.pdfexport.application.PdfExportTxService;
import com.devkor.ifive.nadab.domain.pdfexport.application.event.PdfExportCompletedEvent;
import com.devkor.ifive.nadab.domain.pdfexport.application.event.PdfExportFailedEvent;
import com.devkor.ifive.nadab.domain.pdfexport.application.render.PdfHtmlAssembler;
import com.devkor.ifive.nadab.domain.pdfexport.application.render.PdfImage;
import com.devkor.ifive.nadab.domain.pdfexport.application.render.PdfRenderer;
import com.devkor.ifive.nadab.domain.pdfexport.core.dto.PdfAnswerRowDto;
import com.devkor.ifive.nadab.domain.pdfexport.core.dto.PdfExportRequestedEventDto;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportJob;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportType;
import com.devkor.ifive.nadab.domain.pdfexport.core.repository.PdfExportJobRepository;
import com.devkor.ifive.nadab.domain.pdfexport.core.repository.PdfExportQueryRepository;
import com.devkor.ifive.nadab.domain.pdfexport.infra.PdfExportStorage;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReport;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * PDF 내보내기 비동기 렌더 파이프라인
 * reserve 커밋 후(AFTER_COMMIT) 전용 풀에서 실행: 조회 → 렌더 → 업로드 → confirm.
 * 트랜잭션 경계: 데이터는 짧은 조회 레포로 받아 커넥션을 즉시 반납한 뒤 렌더한다.
 * 핸들러에 @Transactional 을 붙이지 않는다 — 렌더(수 초)가 커넥션을 잡으면 풀이 고갈돼 다른 API까지 대기한다.
 * confirm/failAndRefund는 각자 짧은 트랜잭션(PdfExportTxService)에서 처리한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PdfExportGenerationListener {

    /** 답변 사진 임베드 한 변 px(표시 ~431px의 오버샘플 — 프리뷰 PHOTO_PX와 동일). */
    private static final int PHOTO_PX = 640;

    /** 사진 다운로드 스레드 수. S3 대기는 CPU를 쓰지 않아 렌더 스레드와 코어를 다투지 않는다. */
    private static final int PHOTO_DOWNLOAD_THREADS = 3;

    /** 동시에 들고 있을 원본 사진 장수 — 이 값이 곧 추가 힙(장당 ~136KB). */
    private static final int PHOTO_PREFETCH_WINDOW = 4;

    private final PdfExportJobRepository pdfExportJobRepository;
    private final PdfExportQueryRepository pdfExportQueryRepository;
    private final PdfHtmlAssembler assembler;
    private final PdfRenderer renderer;
    private final PdfExportStorage storage;
    private final PdfExportTxService txService;
    private final PdfExportRenderQueue renderQueue;
    private final ApplicationEventPublisher eventPublisher;

    @Async("pdfExportTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PdfExportRequestedEventDto event) {
        Long jobId = event.jobId();
        Long userId = event.userId();
        Long crystalLogId = event.crystalLogId();

        long startedAt = System.nanoTime();
        int photoCount = 0;
        Path pdfFile = null;
        try {
            // ── 1) 짧은 조회들 (각자 자기 트랜잭션 → 커넥션 즉시 반납). 리포트 엔티티는 detached ──
            //     이후 LAZY user 접근 금지(jsonb content·emotionStats는 컬럼이라 로드됨). 조회는 컬럼만 쓴다.
            PdfExportJob job = pdfExportJobRepository.findById(jobId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.PDF_EXPORT_JOB_NOT_FOUND));
            PdfExportType type = job.getType();
            LocalDate start = job.getStartDate();
            LocalDate end = job.getEndDate();
            String resultKey = job.getResultKey();

            List<PdfAnswerRowDto> answers = type.includesAnswer()
                    ? pdfExportQueryRepository.findAnswersInPeriod(userId, start, end)
                    : List.of();

            List<WeeklyReport> weeklies = List.of();
            List<MonthlyReport> monthlies = List.of();
            List<MonthlyReportV2> monthlyV2s = List.of();
            if (type.includesReport()) {
                // 월간은 V1(레거시)·V2 둘 다 조회. 가드가 한 달 한 버전 보장 → 중복 렌더 0.
                weeklies = pdfExportQueryRepository.findWeeklyReportsInPeriod(userId, start, end);
                monthlies = pdfExportQueryRepository.findMonthlyReportsInPeriod(userId, start, end);
                monthlyV2s = pdfExportQueryRepository.findMonthlyReportsV2InPeriod(userId, start, end);
            }

            // ── 2) Tx/커넥션 밖: 무거운 렌더(S3 사진 다운로드·디코드·차트·HTML·PDF) ──
            //     사진은 assemble 전에 미리 준비한다(null·중복 키는 프리페처가 거른다).
            List<String> imageKeys = answers.stream()
                    .map(PdfAnswerRowDto::imageKey)
                    .toList();
            List<String> skipped = new ArrayList<>();
            Map<String, byte[]> photos = PdfPhotoPrefetcher.prefetch(imageKeys,
                    storage::download,
                    source -> PdfImage.coverSquareJpegBytes(source, PHOTO_PX),
                    (key, e) -> skipPhoto(jobId, skipped, key, e),
                    PHOTO_DOWNLOAD_THREADS, PHOTO_PREFETCH_WINDOW);
            photoCount = photos.size();

            // 준비된 장수 + 스킵된 장수 = 실제로 받아오려 한 사진 수(프리페처가 null·중복을 이미 걸렀다).
            int photosRequested = photoCount + skipped.size();
            if (!skipped.isEmpty()) {
                log.warn("[PDF_EXPORT][PHOTOS_SKIPPED] jobId={}, skipped={}/{}",
                        jobId, skipped.size(), photosRequested);
            }
            requirePhotosWhenExpected(jobId, photosRequested, photos);

            PdfHtmlAssembler.AssembledDocument doc = assembler.assemble(type, answers, weeklies, monthlies,
                    monthlyV2s, key -> Optional.ofNullable(photos.get(key)));
            pdfFile = renderer.render(doc.xhtml(), doc.inlineAssets());

            // ── 3) 업로드(파일명 Content-Disposition 각인) → 완료 확정(markCompleted·completed_at·dedup 자동) ──
            storage.upload(resultKey, pdfFile,
                    PdfExportFileNames.downloadFileName(job),
                    PdfExportFileNames.asciiFallbackFileName(job));
            txService.confirm(jobId, crystalLogId);

            // ── 4) 완료 이벤트(FCM 알림용). 확정 이후라 여기서 실패해도 실패로 되돌리지 않는다 ──
            notifyCompleted(jobId, userId);

        } catch (Exception e) {
            // 렌더·업로드·confirm 어디서 실패해도: 실패 확정 + 환불(별도 Tx). CAS 가드로 이중 환불·공짜 PDF 방지.
            // error_code엔 안전한 ErrorCode enum 이름만 저장 — 예외 메시지·스택은 클라(getStatus)에 노출 금지.
            log.error("[PDF_EXPORT][GENERATION_FAILED] jobId={}, userId={}", jobId, userId, e);
            if (txService.failAndRefund(userId, jobId, crystalLogId,
                    ErrorCode.PDF_EXPORT_GENERATION_FAILED.name())) {
                // 실제로 환불한 경우에만 알린다.
                // false 면 복구 스윕이 먼저 처리한 것이라, 여기서 또 알리면 실패 푸시가 중복된다.
                notifyFailed(jobId, userId);
            }
        } finally {
            // 업로드 성공·실패 무관 로컬 임시파일 정리(S3에 올라간 뒤엔 불필요, 실패 시엔 잔여 제거).
            if (pdfFile != null) {
                deleteQuietly(pdfFile);
            }

            // 성공·실패 무관하게 남긴다(유료 작업이라 감사·환불 문의를 쫓을 수 있어야 한다).
            // pending = 이 시점의 대기 줄 깊이(대기 중 + 렌더 중). 자기 자신을 포함해 최소 1이다.
            log.info("[PDF_EXPORT][RENDER_END] jobId={}, photos={}, elapsedMs={}, pending={}",
                    jobId, photoCount, (System.nanoTime() - startedAt) / 1_000_000L, renderQueue.pending());
        }
    }

    /**
     * 완료 알림 발행. 알림 큐가 포화면 여기서 예외가 나는데, 그때 PDF 는 이미 S3 에 올라가고 과금도 확정된 뒤다.
     * 위 catch 로 흘려보내면 멀쩡히 완료된 작업이 GENERATION_FAILED 로 기록되므로 여기서 삼킨다.
     */
    private void notifyCompleted(Long jobId, Long userId) {
        try {
            eventPublisher.publishEvent(new PdfExportCompletedEvent(jobId, userId));
        } catch (Exception e) {
            log.warn("[PDF_EXPORT][NOTIFY_FAILED] jobId={}, userId={} — PDF 는 정상 완료됐고 알림만 실패했다",
                    jobId, userId, e);
        }
    }

    /**
     * 실패 알림 발행. 이 시점엔 실패 확정·환불이 이미 커밋된 뒤다.
     * 예외가 새면 위 catch 밖으로 나가 원인 예외를 덮으므로 여기서 삼킨다.
     */
    private void notifyFailed(Long jobId, Long userId) {
        try {
            eventPublisher.publishEvent(new PdfExportFailedEvent(jobId, userId));
        } catch (Exception e) {
            log.warn("[PDF_EXPORT][NOTIFY_FAILED] jobId={}, userId={} — 환불은 정상 처리됐고 알림만 실패했다",
                    jobId, userId, e);
        }
    }

    /** 결과 임시파일 삭제(실패해도 삼킨다). */
    private void deleteQuietly(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.warn("[PDF_EXPORT] 결과 임시파일 삭제 실패: {}", file, e);
        }
    }

    /** 사진이 실려야 하는데 한 장도 준비되지 못했으면 job 실패(기존 catch 가 환불까지 처리). 일부 실패는 스킵으로 둔다 */
    private void requirePhotosWhenExpected(Long jobId, int photosRequested, Map<String, byte[]> photos) {
        if (photosRequested > 0 && photos.isEmpty()) {
            // 렌더가 터진 게 아니라 사진 공급이 전멸
            log.error("[PDF_EXPORT][PHOTOS_ALL_FAILED] jobId={}, requested={}", jobId, photosRequested);
            throw new IllegalStateException("답변 사진을 한 장도 준비하지 못했습니다(요청 " + photosRequested + "건)");
        }
    }

    /**
     * 사진 한 장의 다운로드·디코드가 실패했을 때. 그 사진만 건너뛰고 나머지는 렌더한다.
     * S3 장애면 한 렌더에서 수백 장이 같은 이유로 실패하므로 원인(스택)은 첫 건에만 남기고 나머지는 모아서 센다.
     * 프리페처가 다운로드 결과를 호출 스레드에서 순서대로 꺼내며 부르므로 리스트를 그대로 써도 된다.
     */
    private void skipPhoto(Long jobId, List<String> skipped, String imageKey, RuntimeException e) {
        if (skipped.isEmpty()) {
            log.warn("[PDF_EXPORT] 답변 사진 스킵(다운로드/디코드 실패): jobId={}, imageKey={}", jobId, imageKey, e);
        }
        skipped.add(imageKey);
    }
}