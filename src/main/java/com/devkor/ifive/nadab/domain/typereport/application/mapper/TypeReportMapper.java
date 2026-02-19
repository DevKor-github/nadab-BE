package com.devkor.ifive.nadab.domain.typereport.application.mapper;

import com.devkor.ifive.nadab.domain.typereport.api.dto.response.TypeReportResponse;
import com.devkor.ifive.nadab.domain.typereport.core.entity.AnalysisType;
import com.devkor.ifive.nadab.domain.typereport.core.entity.TypeReport;

public final class TypeReportMapper {
    private TypeReportMapper() {};

    public static TypeReportResponse toResponse(TypeReport report, AnalysisType analysisType, String typeImageUrl) {
        // analysisType이 null이면 관련 필드는 null로 설정
        return new TypeReportResponse(
                report.getStatus().name(),
                (analysisType != null) ? analysisType.getName() : null,      // 분석 타입 이름
                (analysisType != null) ? analysisType.getHashtag1() : null,  // 해시태그 1
                (analysisType != null) ? analysisType.getHashtag2() : null,  // 해시태그 2
                (analysisType != null) ? analysisType.getHashtag3() : null,  // 해시태그 3
                report.getTypeAnalysis(),
                report.getPersona1Title(),
                report.getPersona1Content(),
                report.getPersona2Title(),
                report.getPersona2Content(),
                typeImageUrl // 위에서 처리했으므로 그대로 전달 (nullable)
        );
    }
}
