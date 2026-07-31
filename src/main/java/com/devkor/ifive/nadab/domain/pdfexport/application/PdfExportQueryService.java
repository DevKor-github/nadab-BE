package com.devkor.ifive.nadab.domain.pdfexport.application;

import com.devkor.ifive.nadab.domain.pdfexport.api.dto.response.PdfExportArchiveItemResponse;
import com.devkor.ifive.nadab.domain.pdfexport.api.dto.response.PdfExportCurrentResponse;
import com.devkor.ifive.nadab.domain.pdfexport.api.dto.response.PdfExportDownloadResponse;
import com.devkor.ifive.nadab.domain.pdfexport.api.dto.response.PdfExportStatusResponse;
import com.devkor.ifive.nadab.domain.pdfexport.application.helper.PdfExportDownloadRateLimiter;
import com.devkor.ifive.nadab.domain.pdfexport.application.helper.PdfExportFileNames;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportJob;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportStatus;
import com.devkor.ifive.nadab.domain.pdfexport.core.repository.PdfExportJobRepository;
import com.devkor.ifive.nadab.domain.pdfexport.core.repository.PdfExportQueryRepository;
import com.devkor.ifive.nadab.domain.pdfexport.infra.PdfExportStorage;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ConflictException;
import com.devkor.ifive.nadab.global.exception.ForbiddenException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.global.exception.TooManyRequestsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PdfExportQueryService {

    private final PdfExportJobRepository pdfExportJobRepository;
    private final PdfExportQueryRepository pdfExportQueryRepository;
    private final PdfExportStorage pdfExportStorage;
    private final PdfExportDownloadRateLimiter downloadRateLimiter;

    /**
     * 아카이브(이력)에 노출할 상태: 완료(COMPLETED)만. 생성중은 아카이브가 아니라 진행 중 조회(getCurrent)로 다루고,
     * FAILED는 자동 환불된 transient라 제외한다.
     */
    private static final List<PdfExportStatus> ARCHIVE_STATUSES =
            List.of(PdfExportStatus.COMPLETED);

    /** 진행 중으로 보는 상태 — 유저당 이 중 하나가 최대 1개만 존재한다(유니크 인덱스). */
    private static final List<PdfExportStatus> ACTIVE_STATUSES =
            List.of(PdfExportStatus.PENDING, PdfExportStatus.IN_PROGRESS);

    /** 폴링용 상태 조회(상태·유형·기간). 다운로드 발급은 별도 엔드포인트. */
    public PdfExportStatusResponse getStatus(Long userId, Long jobId) {
        PdfExportJob job = findOwnedJob(userId, jobId);
        return new PdfExportStatusResponse(
                job.getId(),
                job.getStatus().name(),
                job.getType().name(),
                job.getStartDate(),
                job.getEndDate(),
                job.getExpiresAt(),
                job.isDownloadExpired(),
                job.getErrorCode());
    }

    /** 다운로드 URL 발급(폴링과 분리, 아카이브도 재사용). 완료 전 409·만료 409·발급 상한 초과 429. */
    public PdfExportDownloadResponse issueDownloadUrl(Long userId, Long jobId) {
        PdfExportJob job = findOwnedJob(userId, jobId);

        if (!job.isDownloadable()) {
            throw new ConflictException(ErrorCode.PDF_EXPORT_NOT_COMPLETED);
        }
        // completed_at 기반 시각 판정 — 우리 만료가 S3 lifecycle 삭제보다 항상 먼저라 죽은 URL 발급 창이 없다.
        if (job.isDownloadExpired()) {
            throw new ConflictException(ErrorCode.PDF_EXPORT_EXPIRED);
        }
        // 발급(= abuse 가능 지점)에만 rate-limit을 건다. 위 검증을 통과한 정상 발급만 카운트.
        if (!downloadRateLimiter.tryAcquire(userId)) {
            throw new TooManyRequestsException(ErrorCode.PDF_EXPORT_DOWNLOAD_RATE_LIMITED);
        }

        // 파일명 Content-Disposition은 업로드 시 S3 객체에 각인됨(CloudFront는 발급 시 오버라이드 불가).
        // 여기선 키만 서명하고, DTO 평문 fileName은 앱 네이티브 저장용으로 동일 유틸에서 조립.
        String fileName = PdfExportFileNames.downloadFileName(job);
        String downloadUrl = pdfExportStorage.generateSignedGetUrl(job.getResultKey());

        return new PdfExportDownloadResponse(downloadUrl, fileName, job.getExpiresAt());
    }

    /**
     * 지금 생성 중인 작업(유저당 최대 1개). 없으면 null. PDF 탭 진입 시 진행 중 작업을 감지해
     * 생성 화면으로 이동하는 데 쓴다. 생성 화면 "포함 내용" 표시용 개수 3종은 즉석 계산해 담는다.
     * 완료 이후 확인은 아카이브(getArchive)로.
     */
    public PdfExportCurrentResponse getCurrent(Long userId) {
        return pdfExportJobRepository.findActiveJob(userId, ACTIVE_STATUSES)
                .map(job -> toCurrentResponse(job, userId))
                .orElse(null);
    }

    private PdfExportCurrentResponse toCurrentResponse(PdfExportJob job, Long userId) {
        LocalDate start = job.getStartDate();
        LocalDate end = job.getEndDate();
        long monthlyCount = pdfExportQueryRepository.countMonthlyReportsV2InPeriod(userId, start, end)
                + pdfExportQueryRepository.countMonthlyReportsInPeriod(userId, start, end);
        return PdfExportCurrentResponse.of(job,
                pdfExportQueryRepository.countAnswersInPeriod(userId, start, end),
                pdfExportQueryRepository.countWeeklyReportsInPeriod(userId, start, end),
                monthlyCount);
    }

    /**
     * 아카이브(이력) 목록: 완료 작업을 최신순(생성순 DESC)으로. 다운로드는 항목별 발급 API(issueDownloadUrl).
     */
    public List<PdfExportArchiveItemResponse> getArchive(Long userId) {
        return pdfExportJobRepository.findArchive(userId, ARCHIVE_STATUSES).stream()
                .map(job -> new PdfExportArchiveItemResponse(
                        job.getId(),
                        job.getType().name(),
                        job.getStartDate(),
                        job.getEndDate(),
                        job.getStatus().name(),
                        job.getExpiresAt(),
                        job.isDownloadExpired()))
                .toList();
    }

    private PdfExportJob findOwnedJob(Long userId, Long jobId) {
        PdfExportJob job = pdfExportJobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PDF_EXPORT_JOB_NOT_FOUND));
        if (!job.getUser().getId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.PDF_EXPORT_ACCESS_FORBIDDEN);
        }
        return job;
    }
}