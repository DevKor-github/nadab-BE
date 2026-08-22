package com.devkor.ifive.nadab.domain.admin.api;

import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminCrystalLogResponse;
import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminLogPageResponse;
import com.devkor.ifive.nadab.domain.admin.application.AdminLogSearchCondition;
import com.devkor.ifive.nadab.domain.admin.application.AdminCrystalLogQueryService;
import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminCrystalLogControllerTest {

    @Mock
    private AdminCrystalLogQueryService adminCrystalLogQueryService;

    @InjectMocks
    private AdminCrystalLogController controller;

    @Test
    void get_logs_passes_page_and_user_filters_to_service() {
        AdminLogPageResponse<AdminCrystalLogResponse> expected =
                new AdminLogPageResponse<>(List.of(), 0, 1, 20, 0, false, false);
        when(adminCrystalLogQueryService.getLogs(org.mockito.ArgumentMatchers.any()))
                .thenReturn(expected);

        ResponseEntity<ApiResponseDto<AdminLogPageResponse<AdminCrystalLogResponse>>> response =
                controller.getLogs(1, 20, " Alice ", " alice@example.com ");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isSameAs(expected);

        ArgumentCaptor<AdminLogSearchCondition> conditionCaptor =
                ArgumentCaptor.forClass(AdminLogSearchCondition.class);
        verify(adminCrystalLogQueryService).getLogs(conditionCaptor.capture());
        assertThat(conditionCaptor.getValue().nickname()).isEqualTo("alice");
        assertThat(conditionCaptor.getValue().email()).isEqualTo("alice@example.com");
        assertThat(conditionCaptor.getValue().page()).isEqualTo(1);
        assertThat(conditionCaptor.getValue().size()).isEqualTo(20);
    }
}
