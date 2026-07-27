package com.devkor.ifive.nadab.domain.pdfexport.application;

import com.devkor.ifive.nadab.domain.pdfexport.application.helper.PdfExportTempFileCleaner;
import com.devkor.ifive.nadab.domain.pdfexport.core.dto.PdfExportReserveResultDto;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportStatus;
import com.devkor.ifive.nadab.domain.pdfexport.support.PdfExportIntegrationTestSupport;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 유료 작업의 정합성 불변식을 실제 DB에서 검증한다. 각 시나리오 끝에서 assertConsistent 로 확인한다.
 */
@Import({PdfExportTxService.class, PdfExportTempFileCleaner.class})
class PdfExportConsistencyTest extends PdfExportIntegrationTestSupport {

    private static final String FAILURE_CODE = ErrorCode.PDF_EXPORT_GENERATION_FAILED.name();

    /** 부팅 때만 도는 빈이라 스프링이 실제로 만들어보는 자리가 여기밖에 없다(java.io.tmpdir 주입 포함). */
    @Autowired
    PdfExportTempFileCleaner tempFileCleaner;

    @Test
    void 임시파일_청소기는_스프링이_주입할_수_있다() {
        assertThat(tempFileCleaner).isNotNull();
    }

    @Test
    void 차감_직후엔_진행중과_대기가_짝을_이룬다() {
        reserve();

        assertConsistent();
        assertThat(balance()).isEqualTo(INITIAL_BALANCE - COST);
    }

    @Test
    void 완료하면_확정이_남고_차감이_유지된다() {
        PdfExportReserveResultDto reserve = reserve();

        txService.confirm(reserve.jobId(), crystalLogIdOf(reserve.jobId()));

        assertThat(statusOf(reserve.jobId())).isEqualTo(PdfExportStatus.COMPLETED);
        assertConsistent();
        assertThat(balance()).isEqualTo(INITIAL_BALANCE - COST);
    }

    @Test
    void 실패하면_환불이_남고_잔액이_복구된다() {
        PdfExportReserveResultDto reserve = reserve();

        txService.failAndRefund(user.getId(), reserve.jobId(), crystalLogIdOf(reserve.jobId()), FAILURE_CODE);

        assertThat(statusOf(reserve.jobId())).isEqualTo(PdfExportStatus.FAILED);
        assertConsistent();
        assertThat(balance()).isEqualTo(INITIAL_BALANCE);
    }

    @Test
    void 완료된_작업은_뒤늦은_환불_요청에도_돈이_돌아가지_않는다() {
        PdfExportReserveResultDto reserve = reserve();
        Long logId = crystalLogIdOf(reserve.jobId());
        txService.confirm(reserve.jobId(), logId);

        // 복구 스윕과 리스너가 겹치는 순서. 막지 못하면 공짜 PDF가 된다.
        txService.failAndRefund(user.getId(), reserve.jobId(), logId, FAILURE_CODE);

        assertThat(statusOf(reserve.jobId())).isEqualTo(PdfExportStatus.COMPLETED);
        assertConsistent();
        assertThat(balance()).isEqualTo(INITIAL_BALANCE - COST);
    }

    @Test
    void 환불된_작업은_뒤늦은_완료_처리에도_확정되지_않는다() {
        PdfExportReserveResultDto reserve = reserve();
        Long logId = crystalLogIdOf(reserve.jobId());
        txService.failAndRefund(user.getId(), reserve.jobId(), logId, FAILURE_CODE);

        // 스윕이 먼저 실패시킨 뒤 느린 렌더가 완료를 밀어 넣는 순서. 확정되면 환불과 결과물을 둘 다 준 셈이 된다.
        txService.confirm(reserve.jobId(), logId);

        assertThat(statusOf(reserve.jobId())).isEqualTo(PdfExportStatus.FAILED);
        assertConsistent();
        assertThat(balance()).isEqualTo(INITIAL_BALANCE);
    }

    @Test
    void 같은_환불을_두_번_요청해도_잔액은_한_번만_돌아온다() {
        PdfExportReserveResultDto reserve = reserve();
        Long logId = crystalLogIdOf(reserve.jobId());

        txService.failAndRefund(user.getId(), reserve.jobId(), logId, FAILURE_CODE);
        txService.failAndRefund(user.getId(), reserve.jobId(), logId, FAILURE_CODE);

        assertConsistent();
        assertThat(balance()).isEqualTo(INITIAL_BALANCE);
    }

    /**
     * 유저당 진행 중 1개를 강제하는 부분 유니크 인덱스(Flyway 가 만든다)가 두 번째 예약을 막는지.
     * 제약 위반이 트랜잭션을 중단시켜 이후 조회가 불가능하므로, 잔액 확인을 먼저 하고 위반을 마지막에 둔다.
     */
    @Test
    void 진행_중인_작업이_있으면_두_번째_예약이_인덱스에_막힌다() {
        reserve();
        assertThat(balance()).isEqualTo(INITIAL_BALANCE - COST);

        assertThatThrownBy(() -> {
            txService.reserveAndPublish(user, TYPE, START, END);
            em.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}