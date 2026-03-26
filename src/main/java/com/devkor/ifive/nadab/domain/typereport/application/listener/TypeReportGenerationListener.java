package com.devkor.ifive.nadab.domain.typereport.application.listener;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReportStatus;
import com.devkor.ifive.nadab.domain.typereport.application.TypeReportTxService;
import com.devkor.ifive.nadab.domain.typereport.application.event.TypeReportCompletedEvent;
import com.devkor.ifive.nadab.domain.typereport.application.helper.TypeEmotionStatsCalculator;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeEmotionStatsContent;
import com.devkor.ifive.nadab.domain.typereport.core.dto.AnalysisTypeCandidateDto;
import com.devkor.ifive.nadab.domain.typereport.core.dto.EvidenceCardDto;
import com.devkor.ifive.nadab.domain.typereport.core.dto.PatternExtractionResultDto;
import com.devkor.ifive.nadab.domain.typereport.core.dto.TypeReportContentDto;
import com.devkor.ifive.nadab.domain.typereport.core.dto.TypeReportGenerationRequestedEventDto;
import com.devkor.ifive.nadab.domain.typereport.core.dto.TypeSelectionResultDto;
import com.devkor.ifive.nadab.domain.typereport.core.entity.TypeReport;
import com.devkor.ifive.nadab.domain.typereport.core.repository.AnalysisTypeRepository;
import com.devkor.ifive.nadab.domain.typereport.core.repository.TypeDailyEntryQueryRepository;
import com.devkor.ifive.nadab.domain.typereport.core.repository.TypeReportRepository;
import com.devkor.ifive.nadab.domain.typereport.core.service.EvidenceCardGenerationService;
import com.devkor.ifive.nadab.domain.typereport.core.service.PatternExtractionService;
import com.devkor.ifive.nadab.domain.typereport.core.service.TypeReportContentGenerationService;
import com.devkor.ifive.nadab.domain.typereport.core.service.TypeSelectionService;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.DailyEntryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TypeReportGenerationListener {

    private final TypeReportTxService typeReportTxService;

    // reportId -> interestCode 얻기 위해 필요 (PENDING row에 interest_code 있음)
    private final TypeReportRepository typeReportRepository;

    // 최근 N개 (interest별) 조회
    private final TypeDailyEntryQueryRepository typeDailyEntryQueryRepository;

    private final AnalysisTypeRepository analysisTypeRepository;

    private final EvidenceCardGenerationService evidenceCardGenerationService;     // Step1
    private final PatternExtractionService patternExtractionService;               // Step2
    private final TypeSelectionService typeSelectionService;                       // Step3
    private final TypeReportContentGenerationService typeReportContentGenerationService; // Step4

    private final ApplicationEventPublisher eventPublisher;

    private static final int RECENT_N = 30;

    @Async("typeReportTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(TypeReportGenerationRequestedEventDto event) {

        // 0) report에서 interest_code 조회
        TypeReport report;
        try {
            report = typeReportRepository.findById(event.reportId())
                    .orElseThrow();
        } catch (Exception e) {
            log.error("[TYPE_REPORT][REPORT_NOT_FOUND] userId={}, reportId={}",
                    event.userId(), event.reportId(), e);

            typeReportTxService.failAndRefundType(event.userId(), event.reportId(), event.crystalLogId());
            return;
        }

        InterestCode interestCode = report.getInterestCode();

        // 1) 최근 N개 DailyEntryDto 조회
        List<DailyEntryDto> recentEntries;
        try {
            recentEntries = typeDailyEntryQueryRepository.findRecentDailyEntriesByInterest(
                    event.userId(),
                    interestCode,
                    PageRequest.of(0, RECENT_N)
            );
        } catch (Exception e) {
            log.error("[TYPE_REPORT][QUERY_FAILED] userId={}, reportId={}, interest={}",
                    event.userId(), event.reportId(), interestCode, e);
            typeReportTxService.failAndRefundType(event.userId(), event.reportId(), event.crystalLogId());
            return;
        }

        // 1-1) 동일 관심사 전체 COMPLETED DailyReport 감정 집계 스냅샷
        TypeEmotionStatsContent emotionStats;
        try {
            emotionStats = TypeEmotionStatsCalculator.calculate(
                    typeDailyEntryQueryRepository.countCompletedEmotionStatsByInterest(
                            event.userId(),
                            interestCode,
                            DailyReportStatus.COMPLETED
                    )
            );
        } catch (Exception e) {
            log.error("[TYPE_REPORT][EMOTION_STATS_FAILED] userId={}, reportId={}, interest={}",
                    event.userId(), event.reportId(), interestCode, e);
            typeReportTxService.failAndRefundType(event.userId(), event.reportId(), event.crystalLogId());
            return;
        }

        // 2) Step1: Evidence Cards 생성
        List<EvidenceCardDto> cards;
        try {
            cards = evidenceCardGenerationService.generate(recentEntries);
        } catch (Exception e) {
            log.error("[TYPE_REPORT][STEP1_FAILED] userId={}, reportId={}, interest={}",
                    event.userId(), event.reportId(), interestCode, e);
            typeReportTxService.failAndRefundType(event.userId(), event.reportId(), event.crystalLogId());
            return;
        }

        // 3) Step2 Patterns Extraction
        PatternExtractionResultDto patterns;
        try {
            patterns = patternExtractionService.extract(cards);
        } catch (Exception e) {
            log.error("[TYPE_REPORT][STEP2_FAILED] userId={}, reportId={}, interest={}",
                    event.userId(), event.reportId(), interestCode, e);
            typeReportTxService.failAndRefundType(event.userId(), event.reportId(), event.crystalLogId());
            return;
        }

        // 4) 후보 6개 타입 로드
        List<AnalysisTypeCandidateDto> candidates;
        try {
            candidates = analysisTypeRepository.findCandidatesByInterestCode(interestCode);
            // size=6 보장 가정. 아니면 여기서 validate.
        } catch (Exception e) {
            log.error("[TYPE_REPORT][CANDIDATES_FAILED] userId={}, reportId={}, interest={}",
                    event.userId(), event.reportId(), interestCode, e);
            typeReportTxService.failAndRefundType(event.userId(), event.reportId(), event.crystalLogId());
            return;
        }

        // 5) Step3: 유형 선택
        TypeSelectionResultDto selection;
        try {
            selection = typeSelectionService.select(candidates, patterns);
        } catch (Exception e) {
            log.error("[TYPE_REPORT][STEP3_FAILED] userId={}, reportId={}, interest={}",
                    event.userId(), event.reportId(), interestCode, e);
            typeReportTxService.failAndRefundType(event.userId(), event.reportId(), event.crystalLogId());
            return;
        }

        // 선택된 1개 후보
        AnalysisTypeCandidateDto selectedType = candidates.stream()
                .filter(c -> c.code().equals(selection.analysisTypeCode()))
                .findFirst()
                .orElse(null);

        if (selectedType == null) {
            log.error("[TYPE_REPORT][STEP3_SELECTED_NOT_FOUND] userId={}, reportId={}, interest={}, code={}",
                    event.userId(), event.reportId(), interestCode, selection.analysisTypeCode());
            typeReportTxService.failAndRefundType(event.userId(), event.reportId(), event.crystalLogId());
            return;
        }

        // 6) Step4: 최종 생성
        TypeReportContentDto content;
        try {
            content = typeReportContentGenerationService.generate(
                    selectedType,
                    patterns,
                    cards,
                    emotionStats,
                    selection.analysisTypeCode()
            );
        } catch (Exception e) {
            log.error("[TYPE_REPORT][STEP4_FAILED] userId={}, reportId={}, interest={}, code={}",
                    event.userId(), event.reportId(), interestCode, selection.analysisTypeCode(), e);
            typeReportTxService.failAndRefundType(event.userId(), event.reportId(), event.crystalLogId());
            return;
        }

        // 7) confirm & 저장
        try {
            typeReportTxService.confirmType(
                    event.reportId(),
                    event.crystalLogId(),
                    event.previousCompletedReportId(),
                    content.analysisTypeCode(),
                    content.typeAnalysis(),
                    content.typeAnalysisContent(),
                    content.emotionSummaryContent(),
                    emotionStats,
                    content.personas().get(0).title(),
                    content.personas().get(0).content(),
                    content.personas().get(1).title(),
                    content.personas().get(1).content()
            );

            // 유형 리포트 완성 이벤트 발행
            String categoryName = getCategoryNameKorean(interestCode);
            eventPublisher.publishEvent(
                new TypeReportCompletedEvent(event.reportId(), event.userId(), categoryName)
            );

        } catch (Exception e) {
            log.error("[TYPE_REPORT][CONFIRM_FAILED] userId={}, reportId={}, crystalLogId={}",
                    event.userId(), event.reportId(), event.crystalLogId(), e);
            typeReportTxService.failAndRefundType(event.userId(), event.reportId(), event.crystalLogId());
        }
    }

    /**
     * InterestCode를 한글 카테고리 이름으로 변환
     */
    private String getCategoryNameKorean(InterestCode code) {
        return switch (code) {
            case PREFERENCE -> "취향";
            case EMOTION -> "감정";
            case ROUTINE -> "루틴";
            case RELATIONSHIP -> "관계";
            case LOVE -> "사랑";
            case VALUES -> "가치관";
        };
    }
}
