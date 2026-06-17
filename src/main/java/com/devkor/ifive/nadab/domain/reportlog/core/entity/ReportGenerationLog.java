package com.devkor.ifive.nadab.domain.reportlog.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.infra.llm.LlmProvider;
import com.devkor.ifive.nadab.global.shared.entity.CreatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.OffsetDateTime;

@Entity
@Table(name = "report_generation_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportGenerationLog extends CreatableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            foreignKey = @ForeignKey(name = "fk_report_generation_logs_user")
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 32)
    private ReportGenerationType reportType;

    @Column(name = "report_id")
    private Long reportId;

    @Enumerated(EnumType.STRING)
    @Column(name = "step", nullable = false, length = 64)
    private ReportGenerationStep step;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ReportGenerationLogStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "llm_provider", length = 32)
    private LlmProvider llmProvider;

    @Column(name = "llm_model", length = 128)
    private String llmModel;

    @Column(name = "error_code", length = 128)
    private String errorCode;

    @Column(name = "exception_class", length = 255)
    private String exceptionClass;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "external_error_code", length = 128)
    private String externalErrorCode;

    @Column(name = "elapsed_ms")
    private Long elapsedMs;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "ended_at")
    private OffsetDateTime endedAt;

    public static ReportGenerationLog start(
            User user,
            ReportGenerationType reportType,
            Long reportId,
            ReportGenerationStep step,
            LlmProvider llmProvider,
            String llmModel
    ) {
        ReportGenerationLog log = new ReportGenerationLog();
        log.user = user;
        log.reportType = reportType;
        log.reportId = reportId;
        log.step = step;
        log.status = ReportGenerationLogStatus.STARTED;
        log.llmProvider = llmProvider;
        log.llmModel = llmModel;
        log.startedAt = OffsetDateTime.now();
        return log;
    }

    public void succeed() {
        this.status = ReportGenerationLogStatus.SUCCEEDED;
        finish();
    }

    public void fail(
            String errorCode,
            String exceptionClass,
            Integer httpStatus,
            String externalErrorCode
    ) {
        this.status = ReportGenerationLogStatus.FAILED;
        this.errorCode = errorCode;
        this.exceptionClass = exceptionClass;
        this.httpStatus = httpStatus;
        this.externalErrorCode = externalErrorCode;
        finish();
    }

    private void finish() {
        this.endedAt = OffsetDateTime.now();
        this.elapsedMs = Duration.between(this.startedAt, this.endedAt).toMillis();
    }
}
