package com.devkor.ifive.nadab.domain.admin.application;

import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminCrystalLogResponse;
import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminLogPageResponse;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLog;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogReason;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogStatus;
import com.devkor.ifive.nadab.domain.wallet.core.repository.CrystalLogRepository;
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
class AdminCrystalLogQueryServiceTest {

    @Mock
    private CrystalLogRepository crystalLogRepository;

    @Test
    void maps_crystal_logs_to_admin_page_response() {
        AdminCrystalLogQueryService service = new AdminCrystalLogQueryService(crystalLogRepository);
        User user = User.createUser("alice@example.com", "hashed_password");
        user.updateNickname("alice");
        CrystalLog log = CrystalLog.createPending(
                user,
                -100L,
                900L,
                CrystalLogReason.REPORT_GENERATE_MONTHLY,
                "MONTHLY_REPORT",
                501L
        );
        AdminLogSearchCondition condition = AdminLogSearchCondition.of(2, 10, "alice", null);
        when(crystalLogRepository.findAllForAdmin(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(log), condition.toPageable(), 11));

        AdminLogPageResponse<AdminCrystalLogResponse> response = service.getLogs(condition);

        assertThat(response.currentPage()).isEqualTo(2);
        assertThat(response.pageSize()).isEqualTo(10);
        assertThat(response.totalCount()).isEqualTo(11);
        assertThat(response.items()).singleElement().satisfies(item -> {
            assertThat(item.user().nickname()).isEqualTo("alice");
            assertThat(item.user().email()).isEqualTo("alice@example.com");
            assertThat(item.delta()).isEqualTo(-100L);
            assertThat(item.balanceAfter()).isEqualTo(900L);
            assertThat(item.reason()).isEqualTo(CrystalLogReason.REPORT_GENERATE_MONTHLY);
            assertThat(item.status()).isEqualTo(CrystalLogStatus.PENDING);
            assertThat(item.refType()).isEqualTo("MONTHLY_REPORT");
            assertThat(item.refId()).isEqualTo(501L);
        });

        ArgumentCaptor<String> nicknameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(crystalLogRepository).findAllForAdmin(
                nicknameCaptor.capture(),
                emailCaptor.capture(),
                pageableCaptor.capture()
        );
        assertThat(nicknameCaptor.getValue()).isEqualTo("alice");
        assertThat(emailCaptor.getValue()).isNull();
        assertThat(pageableCaptor.getValue()).isEqualTo(condition.toPageable());
    }
}
