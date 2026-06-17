package com.devkor.ifive.nadab.domain.reportlog.application;

import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationLog;
import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationLogStatus;
import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationStep;
import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationType;
import com.devkor.ifive.nadab.domain.reportlog.core.repository.ReportGenerationLogRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ai.AiServiceUnavailableException;
import com.devkor.ifive.nadab.global.infra.llm.LlmProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportGenerationLogServiceTest {

    @Mock
    ReportGenerationLogRepository reportGenerationLogRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    ReportGenerationLogService reportGenerationLogService;

    @Test
    void start_saves_started_log() {
        // given
        User user = User.createUser("test@test.com", "hashed_password");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reportGenerationLogRepository.save(org.mockito.ArgumentMatchers.any(ReportGenerationLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        ReportGenerationLog saved = reportGenerationLogService.start(
                1L,
                ReportGenerationType.DAILY,
                100L,
                ReportGenerationStep.DAILY_GENERATE,
                LlmProvider.OPENAI,
                "GPT_4_O_MINI",
                null
        );

        // then
        ArgumentCaptor<ReportGenerationLog> captor = ArgumentCaptor.forClass(ReportGenerationLog.class);
        verify(reportGenerationLogRepository).save(captor.capture());

        assertThat(saved).isSameAs(captor.getValue());
        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getReportType()).isEqualTo(ReportGenerationType.DAILY);
        assertThat(saved.getReportId()).isEqualTo(100L);
        assertThat(saved.getStep()).isEqualTo(ReportGenerationStep.DAILY_GENERATE);
        assertThat(saved.getStatus()).isEqualTo(ReportGenerationLogStatus.STARTED);
        assertThat(saved.getStartedAt()).isNotNull();
    }

    @Test
    void succeed_marks_log_succeeded() {
        // given
        ReportGenerationLog log = startLog();
        when(reportGenerationLogRepository.findById(1L)).thenReturn(Optional.of(log));

        // when
        reportGenerationLogService.succeed(1L);

        // then
        assertThat(log.getStatus()).isEqualTo(ReportGenerationLogStatus.SUCCEEDED);
        assertThat(log.getEndedAt()).isNotNull();
        assertThat(log.getElapsedMs()).isNotNull();
    }

    @Test
    void fail_marks_log_failed_with_external_status() {
        // given
        ReportGenerationLog log = startLog();
        when(reportGenerationLogRepository.findById(1L)).thenReturn(Optional.of(log));

        AiServiceUnavailableException exception = new AiServiceUnavailableException(
                ErrorCode.AI_NO_RESPONSE,
                503,
                "HTTP_503",
                new RuntimeException("provider failed")
        );

        // when
        reportGenerationLogService.fail(1L, exception);

        // then
        assertThat(log.getStatus()).isEqualTo(ReportGenerationLogStatus.FAILED);
        assertThat(log.getErrorCode()).isEqualTo(ErrorCode.AI_NO_RESPONSE.getCode());
        assertThat(log.getExceptionClass()).isEqualTo(AiServiceUnavailableException.class.getName());
        assertThat(log.getHttpStatus()).isEqualTo(503);
        assertThat(log.getExternalErrorCode()).isEqualTo("HTTP_503");
        assertThat(log.getEndedAt()).isNotNull();
        assertThat(log.getElapsedMs()).isNotNull();
    }

    private ReportGenerationLog startLog() {
        return ReportGenerationLog.start(
                null,
                ReportGenerationType.DAILY,
                100L,
                ReportGenerationStep.DAILY_GENERATE,
                LlmProvider.OPENAI,
                "GPT_4_O_MINI",
                null
        );
    }
}
