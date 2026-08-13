package com.devkor.ifive.nadab.domain.pdfexport.application;

import com.devkor.ifive.nadab.domain.pdfexport.application.helper.PdfExportRenderQueue;
import com.devkor.ifive.nadab.domain.pdfexport.application.listener.PdfExportGenerationListener;
import com.devkor.ifive.nadab.domain.pdfexport.application.render.PdfHtmlAssembler;
import com.devkor.ifive.nadab.domain.pdfexport.application.render.PdfRenderer;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportJob;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportStatus;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportType;
import com.devkor.ifive.nadab.domain.pdfexport.core.repository.PdfExportJobRepository;
import com.devkor.ifive.nadab.domain.pdfexport.infra.PdfExportStorage;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLog;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogReason;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogStatus;
import com.devkor.ifive.nadab.domain.wallet.core.entity.UserWallet;
import com.devkor.ifive.nadab.domain.wallet.core.repository.CrystalLogRepository;
import com.devkor.ifive.nadab.domain.wallet.core.repository.UserWalletRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotEnoughCrystalException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.infra.db.PostgresIntegrationTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 트랜잭션 경계가 실제로 커밋·롤백되는지 검증한다.
 * 이 파일만 테스트를 트랜잭션으로 안 감싼다(NOT_SUPPORTED) — 감싸면 서비스 트랜잭션이 참여만 해서
 * "롤백돼서 안 남았다"와 "애초에 커밋된 적이 없다"를 구분할 수 없다.
 * 대신 자동 정리가 없으니 만든 행을 tearDown 에서 직접 지운다. 컨테이너를 공유하는 다른 테스트가
 * job 을 전수 조회해 불변식을 보므로 남기면 그쪽이 깨진다.
 * 리스너도 함께 띄운다. 이 컨텍스트엔 비동기가 꺼져 있어 커밋 직후 같은 스레드에서 실행된다.
 */
@DataJpaTest
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({PdfExportTxService.class, PdfExportGenerationListener.class})
class PdfExportRollbackBoundaryTest extends PostgresIntegrationTestSupport {

    private static final long FUNDED_BALANCE = 1_000L;
    private static final PdfExportType TYPE = PdfExportType.REPORT_AND_ANSWER;
    private static final long COST = TYPE.getCrystalCost();
    private static final LocalDate START = LocalDate.parse("2026-01-01");
    private static final LocalDate END = LocalDate.parse("2026-01-31");
    private static final String RESULT_KEY = "test/pdf-exports/rollback/current.pdf";
    private static final String STALE_KEY = "test/pdf-exports/rollback/stale.pdf";
    private static final String REF_TYPE = "PDF_EXPORT_JOB";

    /** 렌더 협력자는 전부 대역 — 여기서 보는 건 트랜잭션 경계지 렌더 결과가 아니다. */
    @MockitoBean PdfHtmlAssembler assembler;
    @MockitoBean PdfRenderer renderer;
    @MockitoBean PdfExportStorage storage;
    @MockitoBean PdfExportRenderQueue renderQueue;

    /** dedup 실패를 주입하려면 나머지 조회는 진짜로 동작해야 해서 mock 이 아니라 spy 다. */
    @MockitoSpyBean PdfExportJobRepository jobRepository;

    @Autowired PdfExportTxService txService;
    @Autowired UserRepository userRepository;
    @Autowired UserWalletRepository walletRepository;
    @Autowired CrystalLogRepository crystalLogRepository;
    @Autowired PlatformTransactionManager transactionManager;

    /** 픽스처의 벌크 UPDATE(@Modifying)는 호출자 트랜잭션을 요구한다 — 테스트가 비트랜잭션이라 여기서 연다. */
    private TransactionTemplate tx;

    private User fundedUser;
    private User brokeUser;
    private Path renderedFile;

    @BeforeEach
    void setUp() throws IOException {
        tx = new TransactionTemplate(transactionManager);
        when(storage.newResultKey(anyLong())).thenReturn(RESULT_KEY);

        fundedUser = createUser(FUNDED_BALANCE);
        brokeUser = createUser(0L);

        // 리스너가 끝까지 돌 수 있을 만큼만 돌려준다.
        renderedFile = Files.createTempFile("rollback-boundary-", ".pdf");
        when(assembler.assemble(any(), any(), any(), any(), any(), any()))
                .thenReturn(new PdfHtmlAssembler.AssembledDocument("<html/>", Map.of()));
        when(renderer.render(anyString(), any())).thenReturn(renderedFile);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(renderedFile);
        deleteUserData(fundedUser);
        deleteUserData(brokeUser);
    }

    @Test
    void 예약이_실패하면_차감도_job도_남지_않는다() {
        assertThatThrownBy(() -> txService.reserveAndPublish(brokeUser, TYPE, START, END))
                .isInstanceOf(NotEnoughCrystalException.class);

        assertThat(jobsOf(brokeUser)).isEmpty();
        assertThat(crystalLogsOf(brokeUser)).isEmpty();
        assertThat(balanceOf(brokeUser)).isZero();
    }

    @Test
    void 예약이_커밋되면_렌더가_시작된다() {
        txService.reserveAndPublish(fundedUser, TYPE, START, END);

        // 대조군: 이 호출이 있어야 아래 never() 가 "리스너가 안 붙어서 통과"가 아님을 보장한다.
        verify(assembler, times(1)).assemble(any(), any(), any(), any(), any(), any());
    }

    @Test
    void 예약이_롤백되면_렌더가_시작되지_않는다() {
        assertThatThrownBy(() -> txService.reserveAndPublish(brokeUser, TYPE, START, END))
                .isInstanceOf(NotEnoughCrystalException.class);

        verify(assembler, never()).assemble(any(), any(), any(), any(), any(), any());
    }

    @Test
    void dedup이_실패하면_완료_확정이_통째로_롤백된다() {
        Reserved reserved = inProgressJob(fundedUser);

        doThrow(new IllegalStateException("dedup 실패 주입"))
                .when(jobRepository).findStaleCompletedResultKeys(anyLong(), any(), any(), any(), anyLong());

        assertThatThrownBy(() -> txService.confirm(reserved.jobId(), reserved.logId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(statusOf(reserved.jobId())).isEqualTo(PdfExportStatus.IN_PROGRESS);
        assertThat(logStatusOf(reserved.logId())).isEqualTo(CrystalLogStatus.PENDING);
        assertThat(balanceOf(fundedUser)).isEqualTo(FUNDED_BALANCE - COST);
    }

    @Test
    void 환불이_실패하면_실패_표시도_되돌아간다() {
        Reserved reserved = inProgressJob(fundedUser);

        // 지갑을 못 찾는 유저로 호출 — markFailed 가 성공한 뒤 환불에서 터지는 지점.
        assertThatThrownBy(() -> txService.failAndRefund(
                UNKNOWN_USER_ID, reserved.jobId(), reserved.logId(),
                ErrorCode.PDF_EXPORT_GENERATION_FAILED.name()))
                .isInstanceOf(NotFoundException.class);

        assertThat(statusOf(reserved.jobId())).isEqualTo(PdfExportStatus.IN_PROGRESS);
        assertThat(logStatusOf(reserved.logId())).isEqualTo(CrystalLogStatus.PENDING);
        assertThat(balanceOf(fundedUser)).isEqualTo(FUNDED_BALANCE - COST);
    }

    @Test
    void 이전_결과물_삭제는_완료가_커밋된_뒤에_일어난다() {
        Long staleJobId = completedJob(fundedUser);
        Reserved reserved = inProgressJob(fundedUser);

        txService.confirm(reserved.jobId(), reserved.logId());

        verify(storage, times(1)).delete(STALE_KEY);
        assertThat(jobRepository.findById(staleJobId)).isEmpty();
    }

    @Test
    void 완료가_롤백되면_이전_결과물을_지우지_않는다() {
        completedJob(fundedUser);
        Reserved reserved = inProgressJob(fundedUser);

        doThrow(new IllegalStateException("dedup 실패 주입"))
                .when(jobRepository).deleteStaleCompleted(anyLong(), any(), any(), any(), anyLong());

        assertThatThrownBy(() -> txService.confirm(reserved.jobId(), reserved.logId()))
                .isInstanceOf(IllegalStateException.class);

        verify(storage, never()).delete(anyString());
    }

    /* ── 실패 알림 발행 여부를 가르는 반환값 ──────────────────────────── */

    @Test
    void 실제로_환불하면_참을_돌려주고_지갑과_상태를_되돌린다() {
        Reserved reserved = inProgressJob(fundedUser);

        boolean refunded = txService.failAndRefund(fundedUser.getId(), reserved.jobId(), reserved.logId(),
                ErrorCode.PDF_EXPORT_GENERATION_FAILED.name());

        assertThat(refunded).isTrue();
        assertThat(statusOf(reserved.jobId())).isEqualTo(PdfExportStatus.FAILED);
        assertThat(logStatusOf(reserved.logId())).isEqualTo(CrystalLogStatus.REFUNDED);
        assertThat(balanceOf(fundedUser)).isEqualTo(FUNDED_BALANCE);   // 차감분 원복
    }

    @Test
    void 이미_완료된_작업엔_거짓을_돌려준다() {
        Reserved reserved = inProgressJob(fundedUser);
        txService.confirm(reserved.jobId(), reserved.logId());

        // markFailed 경합에서 진 호출(복구 스윕이 뒤늦게 도는 경우)이라 false.
        // true 면 이미 완성된 PDF 에 "생성에 실패했어요" 푸시가 나가므로, 호출자는 이 값으로 알림을 막는다.
        assertThat(txService.failAndRefund(fundedUser.getId(), reserved.jobId(), reserved.logId(),
                ErrorCode.PDF_EXPORT_GENERATION_TIMEOUT.name()))
                .isFalse();
    }

    /* ── 픽스처·조회 ─────────────────────────────────────────────────── */

    private static final Long UNKNOWN_USER_ID = -1L;

    private record Reserved(Long jobId, Long logId) {}

    /**
     * 진행 중 job + 차감 + 대기 로그를 직접 만든다.
     * reserveAndPublish 를 쓰면 커밋 직후 리스너가 이어 돌아 job 이 완료로 바뀌므로, 완료·환불 경계를
     * 보는 테스트는 이벤트 없이 같은 상태를 구성한다.
     */
    private Reserved inProgressJob(User user) {
        return tx.execute(status -> {
            PdfExportJob job = jobRepository.save(
                    PdfExportJob.createPending(user, TYPE, START, END, RESULT_KEY));
            walletRepository.tryConsume(user.getId(), COST);
            CrystalLog log = crystalLogRepository.save(CrystalLog.createPending(
                    user, -COST, FUNDED_BALANCE - COST, CrystalLogReason.PDF_EXPORT_GENERATE, REF_TYPE, job.getId()));
            jobRepository.startProcessing(job.getId(), log.getId());
            return new Reserved(job.getId(), log.getId());
        });
    }

    /** dedup 대상이 될 같은 (유저·유형·기간)의 이전 완료 job. */
    private Long completedJob(User user) {
        return tx.execute(status -> {
            PdfExportJob job = jobRepository.save(
                    PdfExportJob.createPending(user, TYPE, START, END, STALE_KEY));
            jobRepository.startProcessing(job.getId(), null);
            jobRepository.markCompleted(job.getId());
            return job.getId();
        });
    }

    private User createUser(long balance) {
        User user = User.createUser("rollback+" + System.nanoTime() + "@test.com", "hashed_password");
        user.updateNickname("rb" + System.nanoTime());
        User saved = userRepository.save(user);
        walletRepository.save(UserWallet.create(saved, balance));
        return saved;
    }

    private PdfExportStatus statusOf(Long jobId) {
        return jobRepository.findById(jobId).orElseThrow().getStatus();
    }

    private CrystalLogStatus logStatusOf(Long logId) {
        return crystalLogRepository.findById(logId).orElseThrow().getStatus();
    }

    private List<PdfExportJob> jobsOf(User user) {
        return jobRepository.findAll().stream()
                .filter(job -> job.getUser().getId().equals(user.getId()))
                .toList();
    }

    private List<CrystalLog> crystalLogsOf(User user) {
        return crystalLogRepository.findAll().stream()
                .filter(log -> log.getUser().getId().equals(user.getId()))
                .toList();
    }

    private long balanceOf(User user) {
        return walletRepository.findByUserId(user.getId()).orElseThrow().getCrystalBalance();
    }

    /** 참조 순서대로 지운다(job → 크리스탈 로그 → 지갑 → 유저). */
    private void deleteUserData(User user) {
        if (user == null) {
            return;
        }
        jobsOf(user).forEach(job -> jobRepository.deleteById(job.getId()));
        crystalLogsOf(user).forEach(log -> crystalLogRepository.deleteById(log.getId()));
        walletRepository.findByUserId(user.getId())
                .ifPresent(wallet -> walletRepository.deleteById(wallet.getId()));
        userRepository.deleteById(user.getId());
    }
}