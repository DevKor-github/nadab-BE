package com.devkor.ifive.nadab.domain.pdfexport.support;

import com.devkor.ifive.nadab.domain.pdfexport.application.PdfExportTxService;
import com.devkor.ifive.nadab.domain.pdfexport.core.dto.PdfExportReserveResultDto;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportJob;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportStatus;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportType;
import com.devkor.ifive.nadab.domain.pdfexport.core.repository.PdfExportJobRepository;
import com.devkor.ifive.nadab.domain.pdfexport.infra.PdfExportStorage;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLog;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogStatus;
import com.devkor.ifive.nadab.domain.wallet.core.repository.CrystalLogRepository;
import com.devkor.ifive.nadab.domain.wallet.core.repository.UserWalletRepository;
import com.devkor.ifive.nadab.infra.builder.UserBuilder;
import com.devkor.ifive.nadab.infra.builder.UserWalletBuilder;
import com.devkor.ifive.nadab.infra.db.PostgresIntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * PDF 내보내기 정합성 테스트 공통 토대. 잔액 있는 유저 하나를 깔아두고 예약·조회·불변식 검사를 물려준다.
 * 필요한 빈은 각 테스트가 직접 @Import 한다.
 */
@DataJpaTest
@ActiveProfiles("test")
public abstract class PdfExportIntegrationTestSupport extends PostgresIntegrationTestSupport {

    protected static final long INITIAL_BALANCE = 1_000L;
    protected static final PdfExportType TYPE = PdfExportType.REPORT_AND_ANSWER;
    protected static final long COST = TYPE.getCrystalCost();
    protected static final LocalDate START = LocalDate.parse("2026-01-01");
    protected static final LocalDate END = LocalDate.parse("2026-01-31");

    /** 결과 키 각인에만 쓰인다 */
    @MockitoBean
    protected PdfExportStorage storage;

    @Autowired protected PdfExportTxService txService;
    @Autowired protected PdfExportJobRepository jobRepository;
    @Autowired protected CrystalLogRepository crystalLogRepository;
    @Autowired protected UserWalletRepository walletRepository;
    @Autowired protected TestEntityManager em;

    protected User user;

    @BeforeEach
    void setUpFundedUser() {
        when(storage.newResultKey(anyLong())).thenReturn("test/pdf-exports/1/job.pdf");

        user = new UserBuilder(em).build();
        new UserWalletBuilder(em).user(user).build();
        walletRepository.charge(user.getId(), INITIAL_BALANCE);
        em.flush();
    }

    /** 작업 상태와 크리스탈 로그 상태가 짝이 맞는지, 잔액이 원장과 일치하는지 */
    protected void assertConsistent() {
        em.flush();
        em.clear();

        long unrefunded = 0L;
        for (PdfExportJob job : jobRepository.findAll()) {
            CrystalLog log = crystalLogRepository.findById(job.getCrystalLogId()).orElseThrow();
            CrystalLogStatus expected = switch (job.getStatus()) {
                case COMPLETED -> CrystalLogStatus.CONFIRMED;
                case FAILED -> CrystalLogStatus.REFUNDED;
                case PENDING, IN_PROGRESS -> CrystalLogStatus.PENDING;
            };
            assertThat(log.getStatus())
                    .as("jobId=%s(%s) 의 크리스탈 로그 상태", job.getId(), job.getStatus())
                    .isEqualTo(expected);

            if (log.getStatus() != CrystalLogStatus.REFUNDED) {
                unrefunded += -log.getDelta();
            }
        }

        assertThat(balance())
                .as("잔액은 초기 잔액에서 아직 환불되지 않은 차감을 뺀 값이어야 한다")
                .isEqualTo(INITIAL_BALANCE - unrefunded);
    }

    protected PdfExportReserveResultDto reserve() {
        PdfExportReserveResultDto reserve = txService.reserveAndPublish(user, TYPE, START, END);
        em.flush();
        return reserve;
    }

    protected Long crystalLogIdOf(Long jobId) {
        return reload(jobId).getCrystalLogId();
    }

    protected PdfExportStatus statusOf(Long jobId) {
        return reload(jobId).getStatus();
    }

    /** 상태 전이가 전부 벌크 UPDATE라 영속성 컨텍스트를 비우고 DB에서 다시 읽어야 한다. */
    protected PdfExportJob reload(Long jobId) {
        em.flush();
        em.clear();
        return jobRepository.findById(jobId).orElseThrow();
    }

    protected long balance() {
        em.flush();
        em.clear();
        return walletRepository.findByUserId(user.getId()).orElseThrow().getCrystalBalance();
    }
}