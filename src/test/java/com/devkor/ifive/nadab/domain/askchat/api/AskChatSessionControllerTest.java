package com.devkor.ifive.nadab.domain.askchat.api;

import com.devkor.ifive.nadab.domain.askchat.api.dto.request.AskChatQuestionRequest;
import com.devkor.ifive.nadab.domain.askchat.api.dto.request.AskChatSessionStartRequest;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatAnswerGenerationResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatHomeResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatMessageResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatQuestionSendResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatRemainingMessageCountResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatSampleQuestionResponse;
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
    void enterHome_returns_home_display_data_without_creating_session() {
        AskChatSessionController controller = controller();
        UserPrincipal principal = new UserPrincipal(1L);
        AskChatHomeResponse homeResponse = new AskChatHomeResponse(
                9,
                "현진",
                100L,
                List.of(
                        new AskChatSampleQuestionResponse(1L, "VALUES", "나는 어떤 사람이야?"),
                        new AskChatSampleQuestionResponse(2L, "PREFERENCE", "내가 좋아하는 것들의 공통점은 뭐야?"),
                        new AskChatSampleQuestionResponse(3L, "RELATIONSHIP", "어떤 사람과 잘 맞을까?")
                )
        );
        when(askChatSessionService.getHome(1L)).thenReturn(homeResponse);

        ResponseEntity<ApiResponseDto<AskChatHomeResponse>> response = controller.enterHome(principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().remainingMessageCount()).isEqualTo(9);
        assertThat(response.getBody().getData().nickname()).isEqualTo("현진");
        assertThat(response.getBody().getData().crystalBalance()).isEqualTo(100L);
        assertThat(response.getBody().getData().sampleQuestions()).hasSize(3);
        verify(askChatSessionService).getHome(1L);
    }

    @Test
    void startSession_delegates_first_question_to_session_service() {
        AskChatSessionController controller = controller();
        UserPrincipal principal = new UserPrincipal(1L);
        AskChatSessionStartRequest request = new AskChatSessionStartRequest("나는 어떤 사람이야?");
        OffsetDateTime createdAt = OffsetDateTime.of(2026, 7, 21, 10, 0, 0, 0, ZoneOffset.UTC);
        AskChatQuestionSendResponse sendResponse = new AskChatQuestionSendResponse(
                new AskChatSessionResponse(
                        11L,
                        AskChatSessionStatus.ACTIVE,
                        1,
                        15,
                        14
                ),
                new AskChatMessageResponse(
                        100L,
                        AskChatMessageRole.USER,
                        AskChatMessageStatus.COMPLETED,
                        "나는 어떤 사람이야?",
                        createdAt
                ),
                null,
                AskChatAnswerGenerationResponse.failed(
                        ErrorCode.AI_RESPONSE_PARSE_FAILED,
                        "generation failed"
                ),
                9,
                List.of()
        );
        when(askChatSessionService.startSession(1L, "나는 어떤 사람이야?")).thenReturn(sendResponse);

        ResponseEntity<ApiResponseDto<AskChatQuestionSendResponse>> response = controller.startSession(principal, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().session().sessionId()).isEqualTo(11L);
        assertThat(response.getBody().getData().session().remainingTurnCount()).isEqualTo(14);
        assertThat(response.getBody().getData().userMessage().content()).isEqualTo("나는 어떤 사람이야?");
        verify(askChatSessionService).startSession(1L, "나는 어떤 사람이야?");
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
                        14
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
                9,
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
                70L,
                12
        );
        when(askChatWalletChargeService.chargeTurns(1L)).thenReturn(chargeResponse);

        ResponseEntity<ApiResponseDto<AskChatTurnChargeResponse>> response =
                controller.chargeTurns(principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().crystalBalance()).isEqualTo(70L);
        assertThat(response.getBody().getData().remainingMessageCount()).isEqualTo(12);
        verify(askChatWalletChargeService).chargeTurns(1L);
    }

    @Test
    void getRemainingTurns_returns_remaining_message_count_only() {
        AskChatSessionController controller = controller();
        UserPrincipal principal = new UserPrincipal(1L);
        AskChatRemainingMessageCountResponse remainingResponse =
                new AskChatRemainingMessageCountResponse(9);
        when(askChatSessionService.getRemainingMessageCount(1L)).thenReturn(remainingResponse);

        ResponseEntity<ApiResponseDto<AskChatRemainingMessageCountResponse>> response =
                controller.getRemainingTurns(principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().remainingMessageCount()).isEqualTo(9);
        verify(askChatSessionService).getRemainingMessageCount(1L);
    }

    private AskChatSessionController controller() {
        return new AskChatSessionController(
                askChatSessionService,
                askChatMessageCommandService,
                askChatWalletChargeService
        );
    }
}
