package com.devkor.ifive.nadab.domain.pdfexport.application;

import com.devkor.ifive.nadab.domain.pdfexport.api.dto.request.PdfExportStartRequest;
import com.devkor.ifive.nadab.domain.pdfexport.api.dto.response.PdfExportStartResponse;
import com.devkor.ifive.nadab.domain.pdfexport.application.helper.PdfExportRenderQueue;
import com.devkor.ifive.nadab.domain.pdfexport.core.dto.PdfExportReserveResultDto;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportType;
import com.devkor.ifive.nadab.domain.pdfexport.core.repository.PdfExportJobRepository;
import com.devkor.ifive.nadab.domain.pdfexport.core.repository.PdfExportQueryRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.TooManyRequestsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 접수 판정이 차감보다 먼저 일어나는지 검증. 순서가 뒤집히면 크리스탈만 빠지고 렌더는 시작도 안 된 작업이 생긴다.
 * 기간 검증·멱등 재사용 등 나머지 분기는 범위 밖.
 */
class PdfExportServiceTest {

    private static final long USER_ID = 7L;
    private static final long JOB_ID = 100L;
    private static final LocalDate START = LocalDate.parse("2026-01-01");
    private static final LocalDate END = LocalDate.parse("2026-01-31");
    private static final PdfExportStartRequest REQUEST =
            new PdfExportStartRequest(PdfExportType.REPORT_AND_ANSWER, START, END);

    private PdfExportQueryRepository queryRepository;
    private PdfExportTxService txService;
    private PdfExportRenderQueue renderQueue;
    private PdfExportService service;

    @BeforeEach
    void setUp() {
        UserRepository userRepository = mock(UserRepository.class);
        PdfExportJobRepository jobRepository = mock(PdfExportJobRepository.class);
        queryRepository = mock(PdfExportQueryRepository.class);
        txService = mock(PdfExportTxService.class);
        renderQueue = mock(PdfExportRenderQueue.class);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(mock(User.class)));
        when(jobRepository.findActiveJob(eq(USER_ID), any())).thenReturn(Optional.empty());
        // 내보낼 데이터는 있는 상태(각 테스트에서 필요 시 덮어씀).
        when(queryRepository.countAnswersInPeriod(USER_ID, START, END)).thenReturn(30L);

        service = new PdfExportService(userRepository, jobRepository, queryRepository, txService, renderQueue);
    }

    @Test
    void 대기_줄이_꽉_차면_차감하지_않고_429로_거부한다() {
        when(renderQueue.canAccept()).thenReturn(false);

        assertThatThrownBy(() -> service.start(USER_ID, REQUEST))
                .isInstanceOf(TooManyRequestsException.class)
                .hasMessage(ErrorCode.PDF_EXPORT_SERVER_BUSY.getMessage());

        // 차감(reserveAndPublish)이 아예 호출되지 않아야 한다.
        verifyNoInteractions(txService);
    }

    @Test
    void 여유가_있으면_차감하고_접수한다() {
        when(renderQueue.canAccept()).thenReturn(true);
        when(txService.reserveAndPublish(any(), eq(PdfExportType.REPORT_AND_ANSWER), eq(START), eq(END)))
                .thenReturn(new PdfExportReserveResultDto(JOB_ID, 900L));

        PdfExportStartResponse response = service.start(USER_ID, REQUEST);

        assertThat(response.jobId()).isEqualTo(JOB_ID);
        verify(txService).reserveAndPublish(any(), eq(PdfExportType.REPORT_AND_ANSWER), eq(START), eq(END));
    }

    @Test
    void 내보낼_데이터가_없으면_접수_판정까지_가지_않는다() {
        when(queryRepository.countAnswersInPeriod(USER_ID, START, END)).thenReturn(0L);

        assertThatThrownBy(() -> service.start(USER_ID, REQUEST))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(ErrorCode.PDF_EXPORT_NO_DATA.getMessage());

        verifyNoInteractions(txService);
    }
}