package com.devkor.ifive.nadab.domain.pdfexport.application;

import com.devkor.ifive.nadab.domain.pdfexport.api.dto.response.PdfExportCurrentResponse;
import com.devkor.ifive.nadab.domain.pdfexport.api.dto.response.PdfExportDownloadResponse;
import com.devkor.ifive.nadab.domain.pdfexport.application.helper.PdfExportDownloadRateLimiter;
import com.devkor.ifive.nadab.domain.pdfexport.core.dto.PdfExportReserveResultDto;
import com.devkor.ifive.nadab.domain.pdfexport.support.PdfExportIntegrationTestSupport;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ConflictException;
import com.devkor.ifive.nadab.global.exception.ForbiddenException;
import com.devkor.ifive.nadab.global.exception.TooManyRequestsException;
import com.devkor.ifive.nadab.infra.builder.UserBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

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
        assertThat(response.fileName()).isEqualTo("나답_나에게답하다_20260101-20260131.pdf");
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

        Long jobId = reserve().jobId();

        PdfExportCurrentResponse current = queryService.getCurrent(user.getId());
        assertThat(current).isNotNull();
        assertThat(current.jobId()).isEqualTo(jobId);
    }

    /* ── helpers ── */

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