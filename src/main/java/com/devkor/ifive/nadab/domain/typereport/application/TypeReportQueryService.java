package com.devkor.ifive.nadab.domain.typereport.application;

import com.devkor.ifive.nadab.domain.typereport.api.dto.response.MyAllTypeReportsResponse;
import com.devkor.ifive.nadab.domain.typereport.api.dto.response.MyTypeReportResponse;
import com.devkor.ifive.nadab.domain.typereport.api.dto.response.TypeReportResponse;
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

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TypeReportQueryService {

    private final UserRepository userRepository;
    private final TypeReportRepository typeReportRepository;

    private final ProfileImageUrlBuilder imageUrlBuilder;

    public MyTypeReportResponse getMyTypeReport(Long userId, String interestCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        InterestCode code = InterestCode.fromString(interestCode);

        TypeReportResponse reportResponse = typeReportRepository.findByUserIdAndInterestCodeAndStatusAndDeletedAtIsNull(user.getId(), code, TypeReportStatus.COMPLETED)
                .map(report -> {
                    AnalysisType analysisType = report.getAnalysisType();

                    // AnalysisType이 있으면 이미지 URL 생성, 없으면 null 처리
                    String typeImageUrl = (analysisType != null)
                            ? imageUrlBuilder.buildAnalysisTypeImageUrl(analysisType.getCode())
                            : null;

                    return TypeReportMapper.toResponse(report, analysisType, typeImageUrl);
                })
                .orElse(null);

        return new MyTypeReportResponse(reportResponse);
    }

    public MyAllTypeReportsResponse getMyAllTypeReports(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        List<TypeReport> reports = typeReportRepository.findAllActiveWithAnalysisType(user.getId());

        Map<InterestCode, TypeReport> byInterest = new EnumMap<>(InterestCode.class);
        for (TypeReport r : reports) {
            byInterest.put(r.getInterestCode(), r);
        }

        // 항상 6개 key를 내려주고, 없으면 null
        Map<String, TypeReportResponse> result = new LinkedHashMap<>();
        put(result, byInterest, InterestCode.PREFERENCE);
        put(result, byInterest, InterestCode.EMOTION);
        put(result, byInterest, InterestCode.ROUTINE);
        put(result, byInterest, InterestCode.RELATIONSHIP);
        put(result, byInterest, InterestCode.LOVE);
        put(result, byInterest, InterestCode.VALUES);

        return new MyAllTypeReportsResponse(result);
    }

    private void put(Map<String, TypeReportResponse> out, Map<InterestCode, TypeReport> byInterest, InterestCode code) {
        TypeReport report = byInterest.get(code);
        if (report == null) {
            out.put(code.name(), null);
            return;
        }

        AnalysisType analysisType = report.getAnalysisType();
        String typeImageUrl = (analysisType != null)
                ? imageUrlBuilder.buildAnalysisTypeImageUrl(analysisType.getCode())
                : null;

        out.put(code.name(), TypeReportMapper.toResponse(report, analysisType, typeImageUrl));
    }
}
