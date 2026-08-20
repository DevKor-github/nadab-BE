package com.devkor.ifive.nadab.domain.admin.application;

import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminLogPageResponse;
import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminReportGenerationLogResponse;
import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationLog;
import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationStep;
import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationType;
import com.devkor.ifive.nadab.domain.reportlog.core.repository.ReportGenerationLogRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.infra.llm.LlmProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminReportGenerationLogQueryServiceTest {

    @Mock
    private ReportGenerationLogRepository reportGenerationLogRepository;

    @Test
    void maps_report_generation_logs_to_admin_page_response() {
        AdminReportGenerationLogQueryService service =
                new AdminReportGenerationLogQueryService(reportGenerationLogRepository);
        User user = User.createUser("alice@example.com", "hashed_password");
        user.updateNickname("alice");
        ReportGenerationLog log = ReportGenerationLog.start(
                user,
                ReportGenerationType.MONTHLY_V2,
                501L,
                ReportGenerationStep.MONTHLY_V2_TEXT_CONFIRM,
                LlmProvider.OPENAI,
                "GPT_4_O_MINI"
        );
        AdminLogSearchCondition condition = AdminLogSearchCondition.of(2, 10, "alice", null);
        when(reportGenerationLogRepository.findAllForAdmin(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(log), condition.toPageable(), 11));

        AdminLogPageResponse<AdminReportGenerationLogResponse> response = service.getLogs(condition);

        assertThat(response.currentPage()).isEqualTo(2);
        assertThat(response.pageSize()).isEqualTo(10);
        assertThat(response.totalCount()).isEqualTo(11);
        assertThat(response.items()).singleElement().satisfies(item -> {
            assertThat(item.user().nickname()).isEqualTo("alice");
            assertThat(item.user().email()).isEqualTo("alice@example.com");
            assertThat(item.reportType()).isEqualTo(ReportGenerationType.MONTHLY_V2);
            assertThat(item.reportId()).isEqualTo(501L);
            assertThat(item.step()).isEqualTo(ReportGenerationStep.MONTHLY_V2_TEXT_CONFIRM);
        });

        ArgumentCaptor<String> nicknameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(reportGenerationLogRepository).findAllForAdmin(
                nicknameCaptor.capture(),
                emailCaptor.capture(),
                pageableCaptor.capture()
        );
        assertThat(nicknameCaptor.getValue()).isEqualTo("alice");
        assertThat(emailCaptor.getValue()).isNull();
        assertThat(pageableCaptor.getValue()).isEqualTo(condition.toPageable());
    }
}
