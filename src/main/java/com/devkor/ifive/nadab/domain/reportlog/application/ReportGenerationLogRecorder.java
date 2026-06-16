package com.devkor.ifive.nadab.domain.reportlog.application;

import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationLog;
import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationStep;
import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationType;
import com.devkor.ifive.nadab.global.infra.llm.LlmProvider;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportGenerationLogRecorder {

    private final ReportGenerationLogService reportGenerationLogService;

    public Long start(
            Long userId,
            ReportGenerationType reportType,
            Long reportId,
            ReportGenerationStep step,
            LlmProvider llmProvider,
            String llmModel,
            JsonNode metadata
    ) {
        try {
            ReportGenerationLog log = reportGenerationLogService.start(
                    userId,
                    reportType,
                    reportId,
                    step,
                    llmProvider,
                    llmModel,
                    metadata
            );
            return log.getId();
        } catch (Exception e) {
            log.warn("[REPORT_GENERATION_LOG][START_FAILED] reportType={}, step={}, userId={}, reportId={}",
                    reportType, step, userId, reportId, e);
            return null;
        }
    }

    public void succeed(Long logId) {
        if (logId == null) {
            return;
        }

        try {
            reportGenerationLogService.succeed(logId);
        } catch (Exception e) {
            log.warn("[REPORT_GENERATION_LOG][SUCCEED_FAILED] logId={}", logId, e);
        }
    }

    public void fail(Long logId, Exception exception) {
        if (logId == null) {
            return;
        }

        try {
            reportGenerationLogService.fail(logId, exception);
        } catch (Exception e) {
            log.warn("[REPORT_GENERATION_LOG][FAIL_FAILED] logId={}", logId, e);
        }
    }
}
