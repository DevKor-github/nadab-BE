package com.devkor.ifive.nadab.domain.typereport.application;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReportStatus;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.DailyReportRepository;
import com.devkor.ifive.nadab.domain.typereport.api.dto.response.*;
import com.devkor.ifive.nadab.domain.typereport.application.mapper.TypeReportMapper;
import com.devkor.ifive.nadab.domain.typereport.core.entity.AnalysisType;
import com.devkor.ifive.nadab.domain.typereport.core.entity.TypeReport;
import com.devkor.ifive.nadab.domain.typereport.core.entity.TypeReportStatus;
import com.devkor.ifive.nadab.domain.typereport.core.repository.TypeReportRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.user.infra.ProfileImageUrlBuilder;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TypeReportQueryService {

    private final UserRepository userRepository;
    private final TypeReportRepository typeReportRepository;
    private final DailyReportRepository dailyReportRepository;

    private final ProfileImageUrlBuilder imageUrlBuilder;

    private static final int REQUIRED_COUNT = 30;
    private static final long FAILED_VISIBLE_MINUTES = 10;

    public MyTypeReportResponse getMyTypeReport(Long userId, String interestCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        InterestCode code = InterestCode.fromString(interestCode);

        // 1) current: COMPLETED 리포트(없으면 null)
        TypeReportResponse current = typeReportRepository.findByUserIdAndInterestCodeAndStatusAndDeletedAtIsNull(user.getId(), code, TypeReportStatus.COMPLETED)
                .map(report -> {
                    AnalysisType analysisType = report.getAnalysisType();

                    // AnalysisType이 있으면 이미지 URL 생성, 없으면 null 처리
                    String typeImageUrl = (analysisType != null)
                            ? imageUrlBuilder.buildAnalysisTypeImageUrl(analysisType.getCode())
                            : null;

                    return TypeReportMapper.toResponse(report, analysisType, typeImageUrl);
                })
                .orElse(null);

        // 2) generation: IN_PROGRESS 우선, 없으면 최근 FAILED
        TypeReportGenerationResponse generation = resolveGeneration(user.getId(), code);

        // 3) eligibility
        long completedCountLong = dailyReportRepository.countByUserIdAndInterestCodeAndStatus(
                user.getId(), code, DailyReportStatus.COMPLETED
        );
        int completedCount = (completedCountLong > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) completedCountLong;
        boolean canGenerate = completedCount >= REQUIRED_COUNT;

        boolean hasEverCompleted = typeReportRepository.existsByUserIdAndInterestCodeAndStatus(
                user.getId(), code, TypeReportStatus.COMPLETED
        );
        boolean isFirstFree = !hasEverCompleted;

        TypeReportEligibilityResponse eligibility = new TypeReportEligibilityResponse(
                completedCount,
                REQUIRED_COUNT,
                canGenerate,
                isFirstFree
        );

        TypeReportDetailResponse detail = new TypeReportDetailResponse(
                current,
                generation,
                eligibility
        );

        return new MyTypeReportResponse(detail);
    }

    public MyAllTypeReportsResponse getMyAllTypeReports(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        // 1) current: COMPLETED (interest별 0~1개)
        List<TypeReport> completed = typeReportRepository.findAllActiveWithAnalysisType(
                user.getId(), TypeReportStatus.COMPLETED
        );
        Map<InterestCode, TypeReport> completedByInterest = new EnumMap<>(InterestCode.class);
        for (TypeReport r : completed) completedByInterest.put(r.getInterestCode(), r);

        // 2) latest attempt: interest별 최신 1건 (status가 COMPLETED/FAILED/IN_PROGRESS 뭐든 상관없이 최신)
        List<TypeReport> latestAttempts = typeReportRepository.findLatestAttemptsByUser(user.getId());
        Map<InterestCode, TypeReport> latestByInterest = new EnumMap<>(InterestCode.class);
        for (TypeReport r : latestAttempts) latestByInterest.put(r.getInterestCode(), r);

        // 3) eligibility counts(interest별 completed count)
        Map<InterestCode, Integer> completedCounts = buildDailyCompletedCountMap(user.getId());

        // 4) "무료 1회" 판정용: COMPLETED 이력 있는 interestCode 집합
        //     이 집합에 없으면 isFirstFree=true
        List<InterestCode> completedInterestList = typeReportRepository.findDistinctInterestCodesByUserIdAndStatus(
                user.getId(), TypeReportStatus.COMPLETED
        );
        Set<InterestCode> hasEverCompletedSet = EnumSet.noneOf(InterestCode.class);
        hasEverCompletedSet.addAll(completedInterestList);

        // 5) 항상 6개 key 내려주기
        Map<String, TypeReportDetailResponse> result = new LinkedHashMap<>();
        putDetail(result, completedByInterest, latestByInterest, completedCounts, hasEverCompletedSet, InterestCode.PREFERENCE);
        putDetail(result, completedByInterest, latestByInterest, completedCounts, hasEverCompletedSet,InterestCode.EMOTION);
        putDetail(result, completedByInterest, latestByInterest, completedCounts, hasEverCompletedSet,InterestCode.ROUTINE);
        putDetail(result, completedByInterest, latestByInterest, completedCounts, hasEverCompletedSet,InterestCode.RELATIONSHIP);
        putDetail(result, completedByInterest, latestByInterest, completedCounts, hasEverCompletedSet,InterestCode.LOVE);
        putDetail(result, completedByInterest, latestByInterest, completedCounts, hasEverCompletedSet,InterestCode.VALUES);

        return new MyAllTypeReportsResponse(result);
    }

    private void putDetail(
            Map<String, TypeReportDetailResponse> out,
            Map<InterestCode, TypeReport> completedByInterest,
            Map<InterestCode, TypeReport> latestByInterest,
            Map<InterestCode, Integer> completedCounts,
            Set<InterestCode> hasEverCompletedSet,
            InterestCode code
    ) {
        // current
        TypeReport completed = completedByInterest.get(code);
        TypeReportResponse current = null;

        if (completed != null) {
            AnalysisType analysisType = completed.getAnalysisType();
            String typeImageUrl = (analysisType != null)
                    ? imageUrlBuilder.buildAnalysisTypeImageUrl(analysisType.getCode())
                    : null;
            current = TypeReportMapper.toResponse(completed, analysisType, typeImageUrl);
        }

        // generation: latest attempt 기준
        TypeReportGenerationResponse generation = resolveGenerationFromLatest(latestByInterest.get(code));

        // eligibility
        int dailyCompletedCount = completedCounts.getOrDefault(code, 0);
        boolean canGenerate = dailyCompletedCount >= REQUIRED_COUNT;

        // 무료 1회 여부: 과거 COMPLETED 이력 없으면 true
        boolean isFirstFree = !hasEverCompletedSet.contains(code);

        TypeReportEligibilityResponse eligibility = new TypeReportEligibilityResponse(
                dailyCompletedCount,
                REQUIRED_COUNT,
                canGenerate,
                isFirstFree
        );

        out.put(code.name(), new TypeReportDetailResponse(current, generation, eligibility));
    }

    private Map<InterestCode, Integer> buildDailyCompletedCountMap(Long userId) {
        Map<InterestCode, Integer> map = new EnumMap<>(InterestCode.class);

        // 기본 0 세팅(6개 항상)
        for (InterestCode ic : InterestCode.values()) {
            map.put(ic, 0);
        }

        dailyReportRepository.countCompletedByInterest(userId, DailyReportStatus.COMPLETED)
                .forEach(row -> {
                    long c = row.completedCount();
                    int safe = (c > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) c;
                    map.put(row.interestCode(), safe);
                });

        return map;
    }

    private TypeReportGenerationResponse resolveGenerationFromLatest(TypeReport latest) {
        if (latest == null) {
            return new TypeReportGenerationResponse(TypeReportGenerationStatus.NONE, null);
        }

        if (latest.getStatus() == TypeReportStatus.IN_PROGRESS) {
            return new TypeReportGenerationResponse(TypeReportGenerationStatus.IN_PROGRESS, latest.getId());
        }

        if (latest.getStatus() == TypeReportStatus.FAILED && isRecentlyFailed(latest)) {
            return new TypeReportGenerationResponse(TypeReportGenerationStatus.FAILED, latest.getId());
        }

        // latest가 COMPLETED거나, FAILED인데 오래됐으면 NONE
        return new TypeReportGenerationResponse(TypeReportGenerationStatus.NONE, null);
    }

    private TypeReportGenerationResponse resolveGeneration(Long userId, InterestCode code) {
        return typeReportRepository
                .findTopByUserIdAndInterestCodeAndDeletedAtIsNullOrderByCreatedAtDesc(userId, code)
                .map(latest -> {
                    if (latest.getStatus() == TypeReportStatus.IN_PROGRESS) {
                        return new TypeReportGenerationResponse(TypeReportGenerationStatus.IN_PROGRESS, latest.getId());
                    }
                    if (latest.getStatus() == TypeReportStatus.FAILED && isRecentlyFailed(latest)) {
                        return new TypeReportGenerationResponse(TypeReportGenerationStatus.FAILED, latest.getId());
                    }
                    // latest가 COMPLETED면, 생성 상태는 NONE
                    return new TypeReportGenerationResponse(TypeReportGenerationStatus.NONE, null);
                })
                .orElse(new TypeReportGenerationResponse(TypeReportGenerationStatus.NONE, null));
    }


    private boolean isRecentlyFailed(TypeReport report) {

        OffsetDateTime updatedAt = report.getUpdatedAt();
        if (updatedAt == null) return true; // 시각 없으면 일단 노출

        OffsetDateTime threshold = OffsetDateTime.now().minusMinutes(FAILED_VISIBLE_MINUTES);
        return updatedAt.isAfter(threshold);
    }
}
