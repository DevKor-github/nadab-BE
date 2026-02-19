package com.devkor.ifive.nadab.domain.typereport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "나의 유형 리포트 통합 조회 응답")
public record MyAllTypeReportsResponse(

        @Schema(
                description = "interestCode -> TypeReportResponse (단일 조회와 동일 스키마), 없으면 null",
                example = "{\n" +
                        "    \"PREFERENCE\": {\n" +
                        "      \"status\": \"COMPLETED\",\n" +
                        "      \"analysisTypeName\": \"몽글몽글 낭만주의자\",\n" +
                        "      \"hashTag1\": \"#분위기\",\n" +
                        "      \"hashTag2\": \"#취향기록\",\n" +
                        "      \"hashTag3\": \"#나만의색깔\",\n" +
                        "      \"typeAnalysis\": \"...\",\n" +
                        "      \"personaTitle1\": \"...\",\n" +
                        "      \"personaContent1\": \"...\",\n" +
                        "      \"personaTitle2\": \"...\",\n" +
                        "      \"personaContent2\": \"...\",\n" +
                        "      \"typeImageUrl\": \"https://...\"\n" +
                        "    },\n" +
                        "    \"EMOTION\": null,\n" +
                        "    \"ROUTINE\": null,\n" +
                        "    \"RELATIONSHIP\": null,\n" +
                        "    \"LOVE\": null,\n" +
                        "    \"VALUES\": null\n" +
                        "  }\n"
        )
        Map<String, TypeReportResponse> reports
){
}
