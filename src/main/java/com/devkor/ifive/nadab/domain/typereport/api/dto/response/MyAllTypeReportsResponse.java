package com.devkor.ifive.nadab.domain.typereport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "나의 유형 리포트 통합 조회 응답")
public record MyAllTypeReportsResponse(

        @Schema(
                description = "interestCode -> 리포트 상세. 모든 관심 주제 코드 키는 항상 포함되며, 각 값은 최소한 current/generation/eligibility 구조를 가집니다. (current는 없을 수 있음)",
                example =
                        "{\n" +
                                "  \"LOVE\": {\n" +
                                "    \"current\": null,\n" +
                                "    \"generation\": {\n" +
                                "      \"status\": \"NONE\",\n" +
                                "      \"reportId\": null\n" +
                                "    },\n" +
                                "    \"eligibility\": {\n" +
                                "      \"dailyCompletedCount\": 0,\n" +
                                "      \"requiredCount\": 30,\n" +
                                "      \"canGenerate\": false,\n" +
                                "      \"isFirstFree\": true\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"PREFERENCE\": {\n" +
                                "    \"current\": {\n" +
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
                                "    \"generation\": {\n" +
                                "      \"status\": \"NONE\",\n" +
                                "      \"reportId\": null\n" +
                                "    },\n" +
                                "    \"eligibility\": {\n" +
                                "      \"dailyCompletedCount\": 30,\n" +
                                "      \"requiredCount\": 30,\n" +
                                "      \"canGenerate\": true,\n" +
                                "      \"isFirstFree\": false\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"EMOTION\": {\n" +
                                "    \"current\": null,\n" +
                                "    \"generation\": {\n" +
                                "      \"status\": \"IN_PROGRESS\",\n" +
                                "      \"reportId\": 13\n" +
                                "    },\n" +
                                "    \"eligibility\": {\n" +
                                "      \"dailyCompletedCount\": 5,\n" +
                                "      \"requiredCount\": 30,\n" +
                                "      \"canGenerate\": false,\n" +
                                "      \"isFirstFree\": true\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"ROUTINE\": {\n" +
                                "    \"current\": null,\n" +
                                "    \"generation\": {\n" +
                                "      \"status\": \"FAILED\",\n" +
                                "      \"reportId\": 21\n" +
                                "    },\n" +
                                "    \"eligibility\": {\n" +
                                "      \"dailyCompletedCount\": 30,\n" +
                                "      \"requiredCount\": 30,\n" +
                                "      \"canGenerate\": true,\n" +
                                "      \"isFirstFree\": true\n" +
                                "    }\n" +
                                "  }\n" +
                                "}\n" +
                                "(※ 실제 응답에는 LOVE/PREFERENCE/EMOTION/ROUTINE 외에도 RELATIONSHIP/VALUES 키가 동일한 구조로 항상 포함됩니다.)"
        )
        Map<String, TypeReportDetailResponse> reports
){
}
