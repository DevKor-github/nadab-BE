package com.devkor.ifive.nadab.domain.monthlyreport.application;

import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlyContentFactory;
import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlyReportV2ContentFactory;
import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlySocialSummaryContent;
import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlyReportGenerationRequestedEventDtoV2;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlyReportV2Repository;
import com.devkor.ifive.nadab.domain.monthlyreport.core.service.PendingMonthlyReportServiceV2;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeContentFactory;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLog;
import com.devkor.ifive.nadab.domain.wallet.core.entity.UserWallet;
import com.devkor.ifive.nadab.domain.wallet.core.repository.CrystalLogRepository;
import com.devkor.ifive.nadab.domain.wallet.core.repository.UserWalletRepository;
import com.devkor.ifive.nadab.global.shared.util.MonthRangeCalculator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthlyReportTxServiceV2Test {

    @Mock
    private PendingMonthlyReportServiceV2 pendingMonthlyReportServiceV2;
    @Mock
    private MonthlyReportV2Repository monthlyReportV2Repository;
    @Mock
    private UserWalletRepository userWalletRepository;
    @Mock
    private CrystalLogRepository crystalLogRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private ObjectMapper objectMapper;

    private MonthlyReportTxServiceV2 service;

    @BeforeEach
    void setUp() {
        service = new MonthlyReportTxServiceV2(
                pendingMonthlyReportServiceV2,
                monthlyReportV2Repository,
                userWalletRepository,
                crystalLogRepository,
                eventPublisher,
                objectMapper
        );
    }

    @Test
    void 가장_최근_완료된_V2_리포트를_비교_대상으로_확정한다() {
        User user = user(1L);
        MonthlyReportV2 previousReport = report(10L);
        prepareReservation(user, true);
        when(monthlyReportV2Repository
                .findFirstByUserIdAndStatusAndMonthStartDateBeforeOrderByMonthStartDateDesc(
                        1L,
                        MonthlyReportStatus.COMPLETED,
                        MonthRangeCalculator.getLastMonthRange().monthStartDate()
                ))
                .thenReturn(Optional.of(previousReport));

        service.reserveMonthlyAndPublish(user);

        verify(pendingMonthlyReportServiceV2).getOrCreatePendingMonthlyReport(user, true);
        ArgumentCaptor<MonthlyReportGenerationRequestedEventDtoV2> captor =
                ArgumentCaptor.forClass(MonthlyReportGenerationRequestedEventDtoV2.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().previousReportId()).isEqualTo(10L);
    }

    @Test
    void 이전에_완료된_V2_리포트가_없으면_BASELINE으로_확정한다() {
        User user = user(1L);
        prepareReservation(user, false);
        when(monthlyReportV2Repository
                .findFirstByUserIdAndStatusAndMonthStartDateBeforeOrderByMonthStartDateDesc(
                        1L,
                        MonthlyReportStatus.COMPLETED,
                        MonthRangeCalculator.getLastMonthRange().monthStartDate()
                ))
                .thenReturn(Optional.empty());

        service.reserveMonthlyAndPublish(user);

        verify(pendingMonthlyReportServiceV2).getOrCreatePendingMonthlyReport(user, false);
        ArgumentCaptor<MonthlyReportGenerationRequestedEventDtoV2> captor =
                ArgumentCaptor.forClass(MonthlyReportGenerationRequestedEventDtoV2.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().previousReportId()).isNull();
    }

    @Test
    void confirm_monthly_text_serializes_social_summary_in_same_update() throws Exception {
        when(objectMapper.writeValueAsString(any()))
                .thenReturn(
                        "contentJson",
                        "emotionSummaryJson",
                        "emotionStatsJson",
                        "interestStatsJson",
                        "socialSummaryJson"
                );

        service.confirmMonthlyText(
                20L,
                MonthlyReportV2ContentFactory.empty(),
                TypeContentFactory.emptyText(),
                TypeContentFactory.emptyEmotionStats(),
                MonthlyContentFactory.emptyInterestStats(),
                null,
                MonthlySocialSummaryContent.empty(5)
        );

        verify(monthlyReportV2Repository).updateContent(
                eq(20L),
                eq("contentJson"),
                eq("emotionSummaryJson"),
                eq(""),
                eq(""),
                eq(""),
                eq("emotionStatsJson"),
                eq("interestStatsJson"),
                eq(null),
                eq("socialSummaryJson"),
                eq(MonthlyReportStatus.TEXT_COMPLETED.name())
        );
    }

    private void prepareReservation(User user, boolean hasPreviousReport) {
        MonthlyReportV2 pendingReport = report(20L);
        UserWallet wallet = org.mockito.Mockito.mock(UserWallet.class);
        CrystalLog crystalLog = org.mockito.Mockito.mock(CrystalLog.class);

        when(pendingMonthlyReportServiceV2.getOrCreatePendingMonthlyReport(user, hasPreviousReport))
                .thenReturn(pendingReport);
        when(userWalletRepository.tryConsume(1L, 40L)).thenReturn(1);
        when(userWalletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));
        when(wallet.getCrystalBalance()).thenReturn(60L);
        when(crystalLogRepository.save(any(CrystalLog.class))).thenReturn(crystalLog);
        when(crystalLog.getId()).thenReturn(30L);
    }

    private User user(Long id) {
        User user = org.mockito.Mockito.mock(User.class);
        when(user.getId()).thenReturn(id);
        return user;
    }

    private MonthlyReportV2 report(Long id) {
        MonthlyReportV2 report = org.mockito.Mockito.mock(MonthlyReportV2.class);
        when(report.getId()).thenReturn(id);
        return report;
    }
}
