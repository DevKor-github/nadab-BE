package com.devkor.ifive.nadab.domain.pdfexport.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "pdf_export_jobs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PdfExportJob extends AuditableEntity {

    // 다운로드 결과물 보관 기간(완료 시각 기준). 만료되면 URL을 발급해주지 않는다.
    public static final Duration RETENTION = Duration.ofDays(7);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private PdfExportType type;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private PdfExportStatus status;

    @Column(name = "result_key", nullable = false, updatable = false, length = 255)
    private String resultKey;

    @Column(name = "crystal_log_id")
    private Long crystalLogId;

    @Column(name = "error_code", length = 128)
    private String errorCode;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    public static PdfExportJob createPending(User user, PdfExportType type, LocalDate startDate, LocalDate endDate,
                                             String resultKey) {
        PdfExportJob job = new PdfExportJob();
        job.user = user;
        job.type = type;
        job.startDate = startDate;
        job.endDate = endDate;
        job.status = PdfExportStatus.PENDING;
        job.resultKey = resultKey;
        return job;
    }

    // 다운로드 보관 만료 시각(완료 시각 + RETENTION). 완료 전이면 null.
    public OffsetDateTime getExpiresAt() {
        return completedAt == null ? null : completedAt.plus(RETENTION);
    }

    // 보관 기간이 지나 다운로드가 만료됐는지(완료 전이면 false). completed_at 기반 시각 판정(S3 상태 비의존).
    public boolean isDownloadExpired() {
        OffsetDateTime expiresAt = getExpiresAt();
        return expiresAt != null && OffsetDateTime.now().isAfter(expiresAt);
    }

    // 완료돼 결과 파일이 존재하는 상태인지(다운로드 발급 전제). resultKey는 생성 시점부터 존재하므로 status로 판정.
    public boolean isDownloadable() {
        return status == PdfExportStatus.COMPLETED;
    }
}