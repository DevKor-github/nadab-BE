package com.devkor.ifive.nadab.domain.pdfexport.application;

import com.devkor.ifive.nadab.domain.pdfexport.api.dto.request.PdfExportStartRequest;
import com.devkor.ifive.nadab.domain.pdfexport.api.dto.response.PdfExportStartResponse;
import com.devkor.ifive.nadab.domain.pdfexport.application.helper.PdfExportRenderQueue;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportType;
import com.devkor.ifive.nadab.domain.pdfexport.support.PdfExportIntegrationTestSupport;
import com.devkor.ifive.nadab.global.exception.PdfExportInProgressException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * 유저당 동시 생성 1개 정책 검증. 진행 중인 작업이 있을 때의 재요청 처리가 곧 과금 판정이다.
 */
@Import({PdfExportTxService.class, PdfExportService.class})
class PdfExportActiveJobPolicyTest extends PdfExportIntegrationTestSupport {

    /** 대기 줄 판정은 별도 테스트가 덮는다. 여기선 항상 통과시켜 재사용·거부 분기만 본다. */
    @MockitoBean
    PdfExportRenderQueue renderQueue;

    @Autowired PdfExportService service;

    @BeforeEach
    void allowAdmission() {
        when(renderQueue.canAccept()).thenReturn(true);
    }

    @Test
    void 같은_조건_재요청은_추가_차감_없이_같은_작업을_돌려준다() {
        Long jobId = reserve().jobId();

        PdfExportStartResponse response = service.start(user.getId(), request(TYPE, START, END));

        assertThat(response.jobId()).isEqualTo(jobId);
        // balanceAfter=null 은 "이번 요청으로 차감된 게 없다"는 신호다.
        assertThat(response.balanceAfter()).isNull();
        assertThat(balance()).isEqualTo(INITIAL_BALANCE - COST);
        assertConsistent();
    }

    @Test
    void 다른_조건_재요청은_거부하고_차감하지_않는다() {
        Long jobId = reserve().jobId();

        assertThatThrownBy(() -> service.start(user.getId(),
                request(PdfExportType.ANSWER_ONLY, START, END)))
                .isInstanceOf(PdfExportInProgressException.class)
                .satisfies(e -> assertThat(((PdfExportInProgressException) e).getInProgressJob().jobId())
                        .isEqualTo(jobId));

        assertThat(balance()).isEqualTo(INITIAL_BALANCE - COST);
        assertConsistent();
    }

    @Test
    void 기간만_달라도_거부한다() {
        reserve();

        assertThatThrownBy(() -> service.start(user.getId(),
                request(TYPE, START, LocalDate.parse("2026-02-28"))))
                .isInstanceOf(PdfExportInProgressException.class);

        assertThat(balance()).isEqualTo(INITIAL_BALANCE - COST);
    }

    private PdfExportStartRequest request(PdfExportType type, LocalDate start, LocalDate end) {
        return new PdfExportStartRequest(type, start, end);
    }
}