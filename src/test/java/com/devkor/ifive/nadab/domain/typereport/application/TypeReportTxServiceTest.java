package com.devkor.ifive.nadab.domain.typereport.application;

import com.devkor.ifive.nadab.domain.typereport.core.content.TypeContentFactory;
import com.devkor.ifive.nadab.domain.typereport.core.repository.TypeReportRepository;
import com.devkor.ifive.nadab.domain.typereport.core.service.PendingTypeReportService;
import com.devkor.ifive.nadab.domain.wallet.core.repository.CrystalLogRepository;
import com.devkor.ifive.nadab.domain.wallet.core.repository.UserWalletRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ai.AiResponseParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class TypeReportTxServiceTest {

    @Mock
    PendingTypeReportService pendingTypeReportService;

    @Mock
    TypeReportRepository typeReportRepository;

    @Mock
    UserWalletRepository userWalletRepository;

    @Mock
    CrystalLogRepository crystalLogRepository;

    @Mock
    ApplicationEventPublisher eventPublisher;

    TypeReportTxService service;

    @BeforeEach
    void setUp() {
        service = new TypeReportTxService(
                pendingTypeReportService,
                typeReportRepository,
                userWalletRepository,
                crystalLogRepository,
                eventPublisher,
                new ObjectMapper()
        );
    }

    @Test
    void confirmType_rejects_unsupported_marks_tag_before_updating_report() {
        String malformedPersonaContent = "본문 <marks text=\"강조\" marks=[\"BOLD\",\"HIGHLIGHT\"]>";

        assertThatThrownBy(() -> service.confirmType(
                49L,
                1L,
                null,
                "TYPE_A",
                "유형 설명",
                TypeContentFactory.fromPlainText("유형 설명"),
                TypeContentFactory.emptyText(),
                TypeContentFactory.emptyEmotionStats(),
                "내면의 균형",
                malformedPersonaContent,
                "상황 적응 전략",
                "정상 본문"
        )).isInstanceOfSatisfying(AiResponseParseException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TYPE_REPORT_PERSONAS_INVALID));

        verifyNoInteractions(typeReportRepository, crystalLogRepository);
    }
}
