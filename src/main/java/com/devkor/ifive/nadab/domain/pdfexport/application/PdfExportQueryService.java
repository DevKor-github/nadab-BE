package com.devkor.ifive.nadab.domain.pdfexport.application;

import com.devkor.ifive.nadab.domain.pdfexport.api.dto.response.PdfExportArchiveItemResponse;
import com.devkor.ifive.nadab.domain.pdfexport.api.dto.response.PdfExportDownloadResponse;
import com.devkor.ifive.nadab.domain.pdfexport.api.dto.response.PdfExportStatusResponse;
import com.devkor.ifive.nadab.domain.pdfexport.application.helper.PdfExportDownloadRateLimiter;
import com.devkor.ifive.nadab.domain.pdfexport.application.helper.PdfExportFileNames;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportJob;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportStatus;
import com.devkor.ifive.nadab.domain.pdfexport.core.repository.PdfExportJobRepository;
import com.devkor.ifive.nadab.domain.pdfexport.infra.PdfExportStorage;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ConflictException;
import com.devkor.ifive.nadab.global.exception.ForbiddenException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.global.exception.TooManyRequestsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PdfExportQueryService {

    private final PdfExportJobRepository pdfExportJobRepository;
    private final PdfExportStorage pdfExportStorage;
    private final PdfExportDownloadRateLimiter downloadRateLimiter;

    /**
     * 아카이브에 노출할 상태: 생성중(PENDING/IN_PROGRESS) + 완료(COMPLETED). FAILED는 자동 환불된 transient라 제외.
     */
    private static final List<PdfExportStatus> ARCHIVE_STATUSES =
            List.of(PdfExportStatus.PENDING, PdfExportStatus.IN_PROGRESS, PdfExportStatus.COMPLETED);

    /** 폴링용 상태 조회. 다운로드 발급은 별도 엔드포인트. */
    public PdfExportStatusResponse getStatus(Long userId, Long jobId) {
        PdfExportJob job = findOwnedJob(userId, jobId);
        return new PdfExportStatusResponse(
                job.getId(),
                job.getStatus().name(),
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
     * 아카이브(이력) 목록: 생성중·완료 작업을 최신순(생성순 DESC)으로. 다운로드는 항목별 발급 API(issueDownloadUrl).
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