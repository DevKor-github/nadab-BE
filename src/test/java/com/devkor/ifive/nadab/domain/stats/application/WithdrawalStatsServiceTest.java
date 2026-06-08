package com.devkor.ifive.nadab.domain.stats.application;

import com.devkor.ifive.nadab.domain.auth.core.entity.WithdrawalReasonType;
import com.devkor.ifive.nadab.domain.stats.core.dto.withdrawal.WithdrawalEventRowViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.withdrawal.WithdrawalStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.repository.WithdrawalStatsRepository;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WithdrawalStatsServiceTest {

    @Test
    void getWithdrawalStats_groups_latest_rows_by_user_and_withdrawn_at() {
        // given
        WithdrawalStatsRepository repo = mock(WithdrawalStatsRepository.class);
        WithdrawalStatsService service = new WithdrawalStatsService(repo);

        OffsetDateTime withdrawnAt = OffsetDateTime.of(
                2026, 6, 1, 12, 30, 5, 900_000_000, ZoneOffset.UTC
        );
        when(repo.findLatestWithdrawalReasonRows(100)).thenReturn(List.of(
                row(1L, withdrawnAt, "DAILY_LOGGING_BURDEN", null),
                row(1L, withdrawnAt, "OTHER", "  custom reason  "),
                row(2L, Timestamp.valueOf(LocalDateTime.of(2026, 6, 2, 10, 0, 0)), "UNKNOWN_REASON", null)
        ));
        when(repo.countAllWithdrawalReasons()).thenReturn(List.of(
                row("DAILY_LOGGING_BURDEN", 2L),
                row("OTHER", 1L),
                row("UNKNOWN_REASON", 99L)
        ));

        // when
        WithdrawalStatsViewModel vm = service.getWithdrawalStats();

        // then
        assertThat(vm.recentEventLimit()).isEqualTo(100);
        assertThat(vm.eventCount()).isEqualTo(2);
        assertThat(vm.reasonLabels()).hasSize(WithdrawalReasonType.values().length);
        assertThat(vm.reasonCounts()).containsExactly(2L, 0L, 0L, 0L, 0L, 1L);

        WithdrawalEventRowViewModel first = vm.rows().get(0);
        assertThat(first.withdrawnAt()).isEqualTo("2026-06-01 21:30:05");
        assertThat(first.reasons()).contains(", ");
        assertThat(first.customReason()).isEqualTo("custom reason");

        WithdrawalEventRowViewModel second = vm.rows().get(1);
        assertThat(second.reasons()).isEqualTo("UNKNOWN_REASON");
        assertThat(second.customReason()).isEqualTo("-");
    }

    private Object[] row(Object... values) {
        return values;
    }
}
