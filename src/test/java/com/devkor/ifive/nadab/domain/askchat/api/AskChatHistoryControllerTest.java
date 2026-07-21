package com.devkor.ifive.nadab.domain.askchat.api;

import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatHistoryDetailResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatHistoryItemResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatHistoryListResponse;
import com.devkor.ifive.nadab.domain.askchat.application.AskChatHistoryQueryService;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSessionStatus;
import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import com.devkor.ifive.nadab.global.security.principal.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AskChatHistoryControllerTest {

    @Mock
    private AskChatHistoryQueryService askChatHistoryQueryService;

    @Test
    void getHistories_delegates_paging_to_query_service() {
        AskChatHistoryController controller = new AskChatHistoryController(askChatHistoryQueryService);
        UserPrincipal principal = new UserPrincipal(1L);
        OffsetDateTime lastMessageAt = OffsetDateTime.of(2026, 7, 21, 10, 0, 0, 0, ZoneOffset.UTC);
        AskChatHistoryListResponse historiesResponse = new AskChatHistoryListResponse(
                List.of(new AskChatHistoryItemResponse(
                        10L,
                        "first question",
                        "last question",
                        LocalDate.of(2026, 7, 21),
                        AskChatSessionStatus.ACTIVE,
                        2,
                        lastMessageAt
                )),
                false,
                1,
                2,
                20,
                1,
                true,
                false
        );
        when(askChatHistoryQueryService.getHistories(1L, 2, 20)).thenReturn(historiesResponse);

        ResponseEntity<ApiResponseDto<AskChatHistoryListResponse>> response =
                controller.getHistories(principal, 2, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().histories()).hasSize(1);
        assertThat(response.getBody().getData().histories().get(0).lastMessageAt())
                .isEqualTo(lastMessageAt);
        verify(askChatHistoryQueryService).getHistories(1L, 2, 20);
    }

    @Test
    void getHistoryDetail_delegates_user_and_session_to_query_service() {
        AskChatHistoryController controller = new AskChatHistoryController(askChatHistoryQueryService);
        UserPrincipal principal = new UserPrincipal(1L);
        OffsetDateTime createdAt = OffsetDateTime.of(2026, 7, 21, 10, 0, 0, 0, ZoneOffset.UTC);
        AskChatHistoryDetailResponse detailResponse = new AskChatHistoryDetailResponse(
                10L,
                AskChatSessionStatus.ACTIVE,
                2,
                true,
                createdAt,
                null,
                List.of()
        );
        when(askChatHistoryQueryService.getHistoryDetail(1L, 10L)).thenReturn(detailResponse);

        ResponseEntity<ApiResponseDto<AskChatHistoryDetailResponse>> response =
                controller.getHistoryDetail(principal, 10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().sessionId()).isEqualTo(10L);
        assertThat(response.getBody().getData().readOnly()).isTrue();
        verify(askChatHistoryQueryService).getHistoryDetail(1L, 10L);
    }
}
