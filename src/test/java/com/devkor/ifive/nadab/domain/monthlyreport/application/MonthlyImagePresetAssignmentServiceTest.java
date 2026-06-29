package com.devkor.ifive.nadab.domain.monthlyreport.application;

import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyImageStylePreset;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlyReportV2Repository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthlyImagePresetAssignmentServiceTest {

    @Mock
    MonthlyReportV2Repository monthlyReportV2Repository;

    MonthlyImagePresetAssignmentService service;

    @BeforeEach
    void setUp() {
        service = new MonthlyImagePresetAssignmentService(monthlyReportV2Repository);
    }

    @Test
    void reuses_already_assigned_preset() {
        MonthlyReportV2 report = report(1L);
        when(report.getImagePromptVariant()).thenReturn(MonthlyImageStylePreset.INK_WASH);
        when(monthlyReportV2Repository.findById(10L)).thenReturn(Optional.of(report));

        MonthlyImageStylePreset result = service.getOrAssign(1L, 10L);

        assertThat(result).isEqualTo(MonthlyImageStylePreset.INK_WASH);
        verify(monthlyReportV2Repository, never()).findRecentCompletedImagePromptVariants(
                1L, 10L, PageRequest.of(0, 3)
        );
        verify(report, never()).assignImagePromptVariant(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void assigns_preset_excluding_recent_completed_presets() {
        MonthlyReportV2 report = report(1L);
        when(report.getImagePromptVariant()).thenReturn(null);
        when(report.getMonthStartDate()).thenReturn(LocalDate.of(2026, 5, 1));
        when(monthlyReportV2Repository.findById(10L)).thenReturn(Optional.of(report));
        List<MonthlyImageStylePreset> recent = List.of(
                MonthlyImageStylePreset.BOTANICAL_COLLAGE,
                MonthlyImageStylePreset.GLASS_AND_LIGHT,
                MonthlyImageStylePreset.INK_WASH
        );
        when(monthlyReportV2Repository.findRecentCompletedImagePromptVariants(
                1L, 10L, PageRequest.of(0, 3)
        )).thenReturn(recent);

        MonthlyImageStylePreset result = service.getOrAssign(1L, 10L);

        assertThat(recent).doesNotContain(result);
        verify(report).assignImagePromptVariant(result);
    }

    @Test
    void rejects_other_users_report() {
        MonthlyReportV2 report = report(2L);
        when(monthlyReportV2Repository.findById(10L)).thenReturn(Optional.of(report));

        assertThatThrownBy(() -> service.getOrAssign(1L, 10L))
                .isInstanceOfSatisfying(ForbiddenException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MONTHLY_REPORT_ACCESS_FORBIDDEN));
    }

    private MonthlyReportV2 report(Long ownerId) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(ownerId);
        MonthlyReportV2 report = mock(MonthlyReportV2.class);
        when(report.getUser()).thenReturn(user);
        return report;
    }
}
