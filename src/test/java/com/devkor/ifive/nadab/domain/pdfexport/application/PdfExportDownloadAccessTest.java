package com.devkor.ifive.nadab.domain.pdfexport.application;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlyReportV2ContentFactory;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReport;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportComparisonType;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportImageStatus;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2;
import com.devkor.ifive.nadab.domain.pdfexport.api.dto.response.PdfExportCurrentResponse;
import com.devkor.ifive.nadab.domain.pdfexport.api.dto.response.PdfExportDownloadResponse;
import com.devkor.ifive.nadab.domain.pdfexport.application.helper.PdfExportDownloadRateLimiter;
import com.devkor.ifive.nadab.domain.pdfexport.core.dto.PdfExportReserveResultDto;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportStatus;
import com.devkor.ifive.nadab.domain.pdfexport.support.PdfExportIntegrationTestSupport;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReport;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReportStatus;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ConflictException;
import com.devkor.ifive.nadab.global.exception.ForbiddenException;
import com.devkor.ifive.nadab.global.exception.TooManyRequestsException;
import com.devkor.ifive.nadab.global.shared.reportcontent.ReportContentFactory;
import com.devkor.ifive.nadab.infra.builder.UserBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 조회·다운로드 표면 검증. 여기는 거부 경로가 본체다.
 */
@Import({PdfExportTxService.class, PdfExportQueryService.class, PdfExportDownloadRateLimiter.class})
class PdfExportDownloadAccessTest extends PdfExportIntegrationTestSupport {

    private static final String SIGNED_URL = "https://cdn.example.com/local/pdf-exports/7/uuid.pdf?Expires=1";

    @Autowired PdfExportQueryService queryService;

    private User stranger;

    @BeforeEach
    void setUpStranger() {
        when(storage.generateSignedGetUrl(anyString())).thenReturn(SIGNED_URL);
        stranger = new UserBuilder(em).build();
        em.flush();
    }

    @Test
    void 남의_작업은_상태를_조회할_수_없다() {
        Long jobId = reserve().jobId();

        assertThatThrownBy(() -> queryService.getStatus(stranger.getId(), jobId))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(ErrorCode.PDF_EXPORT_ACCESS_FORBIDDEN.getMessage());
    }

    @Test
    void 남의_작업은_다운로드_URL을_발급받을_수_없다() {
        Long jobId = completedJob();

        // 소유 검증이 완료·만료 검사보다 먼저다.
        assertThatThrownBy(() -> queryService.issueDownloadUrl(stranger.getId(), jobId))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void 완료되지_않은_작업은_발급을_거부한다() {
        Long jobId = reserve().jobId();

        assertThatThrownBy(() -> queryService.issueDownloadUrl(user.getId(), jobId))
                .isInstanceOf(ConflictException.class)
                .hasMessage(ErrorCode.PDF_EXPORT_NOT_COMPLETED.getMessage());
    }

    @Test
    void 완료된_작업은_서명_URL과_파일명을_돌려준다() {
        Long jobId = completedJob();

        PdfExportDownloadResponse response = queryService.issueDownloadUrl(user.getId(), jobId);

        assertThat(response.downloadUrl()).isEqualTo(SIGNED_URL);
        assertThat(response.fileName()).isEqualTo("나답_20260101-20260131.pdf");
        assertThat(response.expiresAt()).isNotNull();
    }

    @Test
    void 보관_기간이_지나면_발급을_거부한다() {
        Long jobId = completedJob();
        backdateCompletedAt(jobId, 8);

        // 만료 판정은 S3 객체 존재가 아니라 completed_at 기준 시각이다.
        assertThatThrownBy(() -> queryService.issueDownloadUrl(user.getId(), jobId))
                .isInstanceOf(ConflictException.class)
                .hasMessage(ErrorCode.PDF_EXPORT_EXPIRED.getMessage());
    }

    @Test
    void 발급을_반복하면_상한에서_거부한다() {
        Long jobId = completedJob();
        IntStream.range(0, 20).forEach(i -> queryService.issueDownloadUrl(user.getId(), jobId));

        assertThatThrownBy(() -> queryService.issueDownloadUrl(user.getId(), jobId))
                .isInstanceOf(TooManyRequestsException.class)
                .hasMessage(ErrorCode.PDF_EXPORT_DOWNLOAD_RATE_LIMITED.getMessage());
    }

    @Test
    void 아카이브는_완료된_작업만_노출한다() {
        Long inProgress = reserve().jobId();

        assertThat(queryService.getArchive(user.getId())).isEmpty();

        txService.confirm(inProgress, crystalLogIdOf(inProgress));
        em.flush();
        em.clear();

        assertThat(queryService.getArchive(user.getId()))
                .singleElement()
                .satisfies(item -> assertThat(item.jobId()).isEqualTo(inProgress));
    }

    @Test
    void 진행_중_조회는_생성중인_작업과_포함_개수를_준다() {
        assertThat(queryService.getCurrent(user.getId())).isNull();
        seedPeriodData();

        Long jobId = reserve().jobId();

        PdfExportCurrentResponse current = queryService.getCurrent(user.getId());
        assertThat(current).isNotNull();
        assertThat(current.jobId()).isEqualTo(jobId);
        assertThat(current.type()).isEqualTo(TYPE.name());
        assertThat(current.startDate()).isEqualTo(START);
        assertThat(current.endDate()).isEqualTo(END);
        assertThat(current.status()).isEqualTo(PdfExportStatus.IN_PROGRESS.name());

        // 카운트 3종이 전부 long 이라 자리가 바뀌어도 컴파일된다. 세 값을 다르게 깔아 각자 제자리인지 본다.
        assertThat(current.answerCount()).isEqualTo(3);
        assertThat(current.weeklyCount()).isEqualTo(1);
        assertThat(current.monthlyCount()).isEqualTo(2);
    }

    /* ── helpers ── */

    /**
     * 기간(START~END) 안팎에 답변·리포트를 깔아 카운트가 기간으로 걸러지는지 본다.
     * 월간은 V1(레거시)과 V2 를 함께 두고 합이 나오는지 확인한다 — 실제로는 가드가 한 달에 한 버전만 두지만,
     * 카운트는 두 테이블을 각각 세어 더하므로 한쪽이 빠지면 개수가 줄어든다.
     */
    private void seedPeriodData() {
        DailyQuestion question = em.getEntityManager()
                .createQuery("SELECT q FROM DailyQuestion q ORDER BY q.id", DailyQuestion.class)
                .setMaxResults(1)
                .getSingleResult();

        em.persist(AnswerEntry.create(user, question, "기간 안 답변", START, null));
        em.persist(AnswerEntry.create(user, question, "기간 안 답변", START.plusDays(4), null));
        em.persist(AnswerEntry.create(user, question, "기간 안 답변", END, null));
        em.persist(AnswerEntry.create(user, question, "기간 밖 답변", END.plusDays(5), null));

        // 주간은 걸쳐만 있어도 포함(overlap) — 시작이 기간보다 앞선 주로 깐다.
        em.persist(WeeklyReport.create(user, START.minusDays(3), START.plusDays(3),
                ReportContentFactory.empty(), START.plusDays(3), WeeklyReportStatus.COMPLETED));

        em.persist(MonthlyReport.create(user, START, END,
                ReportContentFactory.empty(), END, MonthlyReportStatus.COMPLETED));
        em.persist(monthlyV2(START, END));
        // 기간과 안 겹치는 지난달 V2 — 월간에도 기간 필터가 걸리는지.
        em.persist(monthlyV2(START.minusMonths(1), START.minusDays(1)));

        em.flush();
        em.clear();
    }

    private MonthlyReportV2 monthlyV2(LocalDate start, LocalDate end) {
        return MonthlyReportV2.create(user, start, end, MonthlyReportV2ContentFactory.empty(), end,
                MonthlyReportStatus.COMPLETED, MonthlyReportImageStatus.COMPLETED,
                MonthlyReportComparisonType.BASELINE);
    }

    private Long completedJob() {
        PdfExportReserveResultDto reserve = reserve();
        txService.confirm(reserve.jobId(), crystalLogIdOf(reserve.jobId()));
        em.flush();
        em.clear();
        return reserve.jobId();
    }

    /** 보관 만료를 만들려면 완료 시각을 과거로 밀어야 한다(markCompleted 가 CURRENT_TIMESTAMP 로 각인). */
    private void backdateCompletedAt(Long jobId, int daysAgo) {
        em.getEntityManager()
                .createQuery("UPDATE PdfExportJob j SET j.completedAt = :movedAt WHERE j.id = :id")
                .setParameter("movedAt", OffsetDateTime.now().minusDays(daysAgo))
                .setParameter("id", jobId)
                .executeUpdate();
        em.clear();
    }
}