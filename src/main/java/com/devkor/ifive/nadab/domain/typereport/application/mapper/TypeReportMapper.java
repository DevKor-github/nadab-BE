package com.devkor.ifive.nadab.domain.typereport.application.mapper;

import com.devkor.ifive.nadab.domain.typereport.api.dto.response.TypeReportResponse;
import com.devkor.ifive.nadab.domain.typereport.core.entity.AnalysisType;
import com.devkor.ifive.nadab.domain.typereport.core.entity.TypeReport;

public class TypeReportMapper {
    private TypeReportMapper() {};

    public static TypeReportResponse toResponse(TypeReport report, AnalysisType analysisType, String typeImageUrl) {
        return new TypeReportResponse(
                analysisType.getName(),
                analysisType.getHashtag1(),
                analysisType.getHashtag2(),
                analysisType.getHashtag3(),
                report.getTypeAnalysis(),
                report.getPersona1Title(),
                report.getPersona1Content(),
                report.getPersona2Title(),
                report.getPersona2Content(),
                typeImageUrl
        );
    }
}
