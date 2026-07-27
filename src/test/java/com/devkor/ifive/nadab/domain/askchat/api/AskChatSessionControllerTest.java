package com.devkor.ifive.nadab.domain.askchat.api;

import com.devkor.ifive.nadab.domain.askchat.api.dto.request.AskChatQuestionRequest;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatAnswerGenerationResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatHistoryItemResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatHomeResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatMessageResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatQuestionSendResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatSessionResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatTurnChargeResponse;
import com.devkor.ifive.nadab.domain.askchat.application.AskChatMessageCommandService;
import com.devkor.ifive.nadab.domain.askchat.application.AskChatSessionService;
import com.devkor.ifive.nadab.domain.askchat.application.AskChatWalletChargeService;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageRole;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageStatus;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSessionStatus;
import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
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
class AskChatSessionControllerTest {

    @Mock
    private AskChatSessionService askChatSessionService;

    @Mock
    private AskChatMessageCommandService askChatMessageCommandService;

    @Mock
    private AskChatWalletChargeService askChatWalletChargeService;

    @Test
    void enterHome_returns_recent_sessions_without_creating_session() {
        AskChatSessionController controller = controller();
        UserPrincipal principal = new UserPrincipal(1L);
        OffsetDateTime lastMessageAt = OffsetDateTime.of(2026, 7, 21, 10, 0, 0, 0, ZoneOffset.UTC);
        AskChatHomeResponse homeResponse = AskChatHomeResponse.of(
                15,
                List.of(new AskChatHistoryItemResponse(
                        10L,
                        "first question",
                        "last question",
                        LocalDate.of(2026, 7, 21),
                        AskChatSessionStatus.ACTIVE,
                        2,
                        lastMessageAt
                ))
        );
        when(askChatSessionService.getHome(1L)).thenReturn(homeResponse);

        ResponseEntity<ApiResponseDto<AskChatHomeResponse>> response = controller.enterHome(principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().recentSessions()).hasSize(1);
        assertThat(response.getBody().getData().recentSessionsEmpty()).isFalse();
        assertThat(response.getBody().getData().recentSessions().get(0).lastMessageAt())
                .isEqualTo(lastMessageAt);
        verify(askChatSessionService).getHome(1L);
    }

    @Test
    void startSession_delegates_to_session_service_each_time() {
        AskChatSessionController controller = controller();
        UserPrincipal principal = new UserPrincipal(1L);
        OffsetDateTime createdAt = OffsetDateTime.of(2026, 7, 21, 10, 0, 0, 0, ZoneOffset.UTC);
        AskChatSessionResponse sessionResponse = new AskChatSessionResponse(
                11L,
                AskChatSessionStatus.ACTIVE,
                0,
                15,
                15,
                createdAt,
                null
        );
        when(askChatSessionService.startSession(1L)).thenReturn(sessionResponse);

        ResponseEntity<ApiResponseDto<AskChatSessionResponse>> response = controller.startSession(principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().sessionId()).isEqualTo(11L);
        assertThat(response.getBody().getData().remainingTurnCount()).isEqualTo(15);
        verify(askChatSessionService).startSession(1L);
    }

    @Test
    void sendQuestion_delegates_user_session_and_content_to_message_service() {
        AskChatSessionController controller = controller();
        UserPrincipal principal = new UserPrincipal(1L);
        AskChatQuestionRequest request = new AskChatQuestionRequest(10L, "question");
        OffsetDateTime createdAt = OffsetDateTime.of(2026, 7, 21, 10, 0, 0, 0, ZoneOffset.UTC);
        AskChatQuestionSendResponse sendResponse = new AskChatQuestionSendResponse(
                new AskChatSessionResponse(
                        10L,
                        AskChatSessionStatus.ACTIVE,
                        1,
                        15,
                        14,
                        createdAt,
                        null
                ),
                new AskChatMessageResponse(
                        100L,
                        AskChatMessageRole.USER,
                        AskChatMessageStatus.COMPLETED,
                        "question",
                        createdAt
                ),
                null,
                AskChatAnswerGenerationResponse.failed(
                        ErrorCode.AI_RESPONSE_PARSE_FAILED,
                        "generation failed"
                ),
                List.of()
        );
        when(askChatMessageCommandService.sendQuestion(1L, 10L, "question")).thenReturn(sendResponse);

        ResponseEntity<ApiResponseDto<AskChatQuestionSendResponse>> response =
                controller.sendQuestion(principal, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().assistantMessage()).isNull();
        assertThat(response.getBody().getData().answerGeneration().success()).isFalse();
        assertThat(response.getBody().getData().answerGeneration().errorCode())
                .isEqualTo(ErrorCode.AI_RESPONSE_PARSE_FAILED.getCode());
        verify(askChatMessageCommandService).sendQuestion(1L, 10L, "question");
    }

    @Test
    void chargeTurns_delegates_to_wallet_charge_service() {
        AskChatSessionController controller = controller();
        UserPrincipal principal = new UserPrincipal(1L);
        AskChatTurnChargeResponse chargeResponse = new AskChatTurnChargeResponse(
                10,
                200L,
                70L,
                2,
                10,
                12
        );
        when(askChatWalletChargeService.chargeTurns(1L)).thenReturn(chargeResponse);

        ResponseEntity<ApiResponseDto<AskChatTurnChargeResponse>> response =
                controller.chargeTurns(principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().chargedTurnCount()).isEqualTo(10);
        assertThat(response.getBody().getData().crystalCost()).isEqualTo(200L);
        assertThat(response.getBody().getData().crystalBalance()).isEqualTo(70L);
        assertThat(response.getBody().getData().totalTurnBalance()).isEqualTo(12);
        verify(askChatWalletChargeService).chargeTurns(1L);
    }

    private AskChatSessionController controller() {
        return new AskChatSessionController(
                askChatSessionService,
                askChatMessageCommandService,
                askChatWalletChargeService
        );
    }
}
