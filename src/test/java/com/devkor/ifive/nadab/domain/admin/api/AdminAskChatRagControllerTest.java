package com.devkor.ifive.nadab.domain.admin.api;

import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminAskChatRagBackfillStatusResponse;
import com.devkor.ifive.nadab.domain.askchat.application.AskChatRagBackfillService;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatRagBackfillStatusDto;
import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAskChatRagControllerTest {

    @Mock
    private AskChatRagBackfillService askChatRagBackfillService;

    @InjectMocks
    private AdminAskChatRagController controller;

    @Test
    void getDailyAnswerBackfillStatus_returns_current_counts() {
        when(askChatRagBackfillService.getCompletedDailyAnswerStatus())
                .thenReturn(new AskChatRagBackfillStatusDto(4, 12, 2));

        ResponseEntity<ApiResponseDto<AdminAskChatRagBackfillStatusResponse>> response =
                controller.getDailyAnswerBackfillStatus();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData())
                .isEqualTo(new AdminAskChatRagBackfillStatusResponse(4, 12, 2));
    }
}
