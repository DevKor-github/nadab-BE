package com.devkor.ifive.nadab.domain.pdfexport.application;

import com.devkor.ifive.nadab.domain.pdfexport.api.dto.request.PdfExportStartRequest;
import com.devkor.ifive.nadab.domain.pdfexport.api.dto.response.PdfExportPreviewResponse;
import com.devkor.ifive.nadab.domain.pdfexport.api.dto.response.PdfExportStartResponse;
import com.devkor.ifive.nadab.domain.pdfexport.application.helper.PdfExportRenderQueue;
import com.devkor.ifive.nadab.domain.pdfexport.core.dto.PdfExportReserveResultDto;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportJob;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportStatus;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportType;
import com.devkor.ifive.nadab.domain.pdfexport.core.repository.PdfExportJobRepository;
import com.devkor.ifive.nadab.domain.pdfexport.core.repository.PdfExportQueryRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.global.exception.PdfExportInProgressException;
import com.devkor.ifive.nadab.global.exception.TooManyRequestsException;
import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PdfExportService {

    private final UserRepository userRepository;
    private final PdfExportJobRepository pdfExportJobRepository;
    private final PdfExportQueryRepository pdfExportQueryRepository;
    private final PdfExportTxService pdfExportTxService;
    private final PdfExportRenderQueue renderQueue;

    /** 종료일 기준 최대 1년 (윤년 포함 포괄 범위) */
    private static final long MAX_PERIOD_DAYS = 366L;

    /** 진행 중으로 보는 상태 — 유저당 이 중 하나가 최대 1개만 존재한다(유니크 인덱스). */
    private static final List<PdfExportStatus> ACTIVE_STATUSES =
            List.of(PdfExportStatus.PENDING, PdfExportStatus.IN_PROGRESS);

    /**
     * 비동기 시작 API: 즉시 jobId 반환. 상태는 GET 폴링으로 확인.
     * 유저당 동시 생성은 1개 — 진행 중 작업이 있으면 같은 조건은 재과금 없이 재사용하고,
     * 다른 조건이면 409(PDF_EXPORT_ALREADY_IN_PROGRESS)로 거부한다.
     */
    public PdfExportStartResponse start(Long userId, PdfExportStartRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        PdfExportType type = request.type();
        LocalDate startDate = request.startDate();
        LocalDate endDate = request.endDate();

        validatePeriod(startDate, endDate);

        Optional<PdfExportJob> active = pdfExportJobRepository.findActiveJob(userId, ACTIVE_STATUSES);
        if (active.isPresent()) {
            return resolveActive(active.get(), type, startDate, endDate);
        }

        // 차감 전 데이터 존재 검사: 빈 PDF에 과금 방지(신규 차감 경로만, 재사용/거부는 위에서 반환)
        if (!hasExportableData(userId, type, startDate, endDate)) {
            throw new BadRequestException(ErrorCode.PDF_EXPORT_NO_DATA);
        }

        // 대기 줄이 밀려 있으면 거부(차감 0). 차감 뒤에 실행기가 거부하면 돈만 빠지고 렌더는 시작조차 안 된다.
        if (!renderQueue.canAccept()) {
            throw new TooManyRequestsException(ErrorCode.PDF_EXPORT_SERVER_BUSY);
        }

        // (Tx) job(PENDING) + 선차감 + 로그 + IN_PROGRESS + publish
        try {
            PdfExportReserveResultDto reserve = pdfExportTxService.reserveAndPublish(user, type, startDate, endDate);
            return new PdfExportStartResponse(reserve.jobId(), PdfExportStatus.PENDING.name(), reserve.balanceAfter());
        } catch (DataIntegrityViolationException e) {
            // 진짜 동시 요청: 둘 다 findActiveJob을 통과한 뒤 INSERT가 겹치면 부분 유니크 인덱스
            // (uq_pdf_export_jobs_active_user)가 두 번째 INSERT를 막는다. 차감(tryConsume) 전 단계라 이중과금은 0.
            // 먼저 커밋된 진행 중 job으로 수렴 — 같은 조건이면 재사용, 다르면 거부.
            PdfExportJob job = pdfExportJobRepository.findActiveJob(userId, ACTIVE_STATUSES)
                    .orElseThrow(() -> e);
            return resolveActive(job, type, startDate, endDate);
        }
    }

    /**
     * 진행 중 작업이 이미 있을 때의 처리 — 같은 조건이면 재과금 없이 재사용, 다른 조건이면 거부.
     * 재사용 응답은 status=PENDING(접수됨 신호, 실제 상태는 GET 폴링)·balanceAfter=null(추가 차감 없음).
     * 거부(409) 응답엔 진행 중 작업 id를 실어, 클라가 그 생성 화면으로 이동할 수 있게 한다(상세·개수는 GET /current).
     */
    private PdfExportStartResponse resolveActive(PdfExportJob active, PdfExportType type,
                                                 LocalDate startDate, LocalDate endDate) {
        boolean sameRequest = active.getType() == type
                && active.getStartDate().equals(startDate)
                && active.getEndDate().equals(endDate);
        if (sameRequest) {
            return new PdfExportStartResponse(active.getId(), PdfExportStatus.PENDING.name(), null);
        }
        throw new PdfExportInProgressException(active.getId());
    }

    /**
     * 생성/차감 전 미리보기: 기간 내 답변·주간·월간 개수(유형 무관 3종). 차감·렌더와 무관한 순수 조회.
     * 기간 검증은 start()와 동일(잘못된 기간은 같은 에러)
     */
    public PdfExportPreviewResponse preview(Long userId, LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);
        // 월간은 V2 + 레거시 V1 합산(한 달 한 버전이라 중복 없음).
        long monthlyCount = pdfExportQueryRepository.countMonthlyReportsV2InPeriod(userId, startDate, endDate)
                + pdfExportQueryRepository.countMonthlyReportsInPeriod(userId, startDate, endDate);
        return new PdfExportPreviewResponse(
                pdfExportQueryRepository.countAnswersInPeriod(userId, startDate, endDate),
                pdfExportQueryRepository.countWeeklyReportsInPeriod(userId, startDate, endDate),
                monthlyCount);
    }

    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new BadRequestException(ErrorCode.PDF_EXPORT_INVALID_PERIOD);
        }
        // 미래 기록은 존재할 수 없음 → 종료일 상한은 오늘(KST). 미래 범위로 빈 PDF 과금 방지.
        if (endDate.isAfter(TodayDateTimeProvider.getTodayDate())) {
            throw new BadRequestException(ErrorCode.PDF_EXPORT_INVALID_PERIOD);
        }
        long inclusiveDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (inclusiveDays > MAX_PERIOD_DAYS) {
            throw new BadRequestException(ErrorCode.PDF_EXPORT_INVALID_PERIOD);
        }
    }

    /**
     * 유형별로 내보낼 데이터가 하나라도 있는지(차감 전 검사). 첫 히트에서 단락.
     * - 답변 포함 유형: 기간 내 답변
     * - 리포트 포함 유형: 기간과 겹치는 완료 주간/월간
     */
    private boolean hasExportableData(Long userId, PdfExportType type, LocalDate startDate, LocalDate endDate) {
        if (type.includesAnswer()
                && pdfExportQueryRepository.countAnswersInPeriod(userId, startDate, endDate) > 0) {
            return true;
        }
        if (type.includesReport()) {
            return pdfExportQueryRepository.countWeeklyReportsInPeriod(userId, startDate, endDate) > 0
                    || pdfExportQueryRepository.countMonthlyReportsV2InPeriod(userId, startDate, endDate) > 0
                    || pdfExportQueryRepository.countMonthlyReportsInPeriod(userId, startDate, endDate) > 0;
        }
        return false;
    }
}