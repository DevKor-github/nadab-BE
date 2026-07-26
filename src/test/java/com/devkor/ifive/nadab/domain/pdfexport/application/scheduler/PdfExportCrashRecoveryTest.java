package com.devkor.ifive.nadab.domain.pdfexport.application.scheduler;

import com.devkor.ifive.nadab.domain.pdfexport.application.PdfExportTxService;
import com.devkor.ifive.nadab.domain.pdfexport.core.dto.PdfExportReserveResultDto;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportJob;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportStatus;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportType;
import com.devkor.ifive.nadab.domain.pdfexport.support.PdfExportIntegrationTestSupport;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 렌더가 시작조차 못 한 채 남은 작업을 복구 스윕이 회수하는지 실제 DB에서 검증한다.
 * 렌더 리스너가 이 슬라이스에 없어 예약 이벤트를 아무도 받지 않는다 = 대기 큐가 증발한 것과 같은 상태가 된다.
 */
@Import({PdfExportTxService.class, PdfExportRecoveryScheduler.class})
class PdfExportCrashRecoveryTest extends PdfExportIntegrationTestSupport {

    @Autowired
    PdfExportRecoveryScheduler scheduler;

    @Test
    void 렌더가_시작조차_못한_작업을_실패처리하고_환불한다() {
        Long jobId = reserveAndCrash(Duration.ofMinutes(61));

        scheduler.recoverStuckJobs();

        PdfExportJob job = reload(jobId);
        assertThat(job.getStatus()).isEqualTo(PdfExportStatus.FAILED);
        assertThat(job.getErrorCode()).isEqualTo("PDF_EXPORT_GENERATION_TIMEOUT");
        assertThat(crystalLogRepository.findById(job.getCrystalLogId()).orElseThrow().getStatus())
                .isEqualTo(CrystalLogStatus.REFUNDED);
        assertConsistent();
        assertThat(balance()).isEqualTo(INITIAL_BALANCE);
    }

    @Test
    void 두_번_쓸어도_이중_환불이_없다() {
        reserveAndCrash(Duration.ofMinutes(61));

        scheduler.recoverStuckJobs();
        scheduler.recoverStuckJobs();

        // 두 번째 스윕은 조회에서 이미 빠진다(FAILED). 설령 다시 불려도 markFailed가 0을 반환해 환불하지 않는다.
        assertConsistent();
        assertThat(balance()).isEqualTo(INITIAL_BALANCE);
    }

    @Test
    void 아직_진행_중일_수_있는_작업은_건드리지_않는다() {
        Long jobId = reserveAndCrash(Duration.ofMinutes(10));

        scheduler.recoverStuckJobs();

        // 임계 안쪽이면 큐에서 대기 중이거나 렌더 중일 수 있다. 여기서 취소하면 멀쩡한 유료 작업을 죽인다.
        PdfExportJob job = reload(jobId);
        assertThat(job.getStatus()).isEqualTo(PdfExportStatus.IN_PROGRESS);
        assertThat(job.getErrorCode()).isNull();
        assertConsistent();
        assertThat(balance()).isEqualTo(INITIAL_BALANCE - COST);
    }

    /** 차감까지 커밋된 작업을 만들고, 큐 진입 시각을 age만큼 과거로 밀어 "그 뒤로 아무 일도 안 일어난" 상태를 만든다. */
    private Long reserveAndCrash(Duration age) {
        PdfExportReserveResultDto reserve = reserve();
        assertThat(balance()).isEqualTo(INITIAL_BALANCE - COST);

        // 벌크 UPDATE는 @PreUpdate를 타지 않아 updatedAt을 과거로 밀 수 있다(엔티티로 저장하면 now()로 덮인다).
        em.getEntityManager()
                .createQuery("UPDATE PdfExportJob j SET j.updatedAt = :movedAt WHERE j.id = :id")
                .setParameter("movedAt", OffsetDateTime.now().minus(age))
                .setParameter("id", reserve.jobId())
                .executeUpdate();
        em.clear();
        return reserve.jobId();
    }
}