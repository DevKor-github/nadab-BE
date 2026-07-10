package com.devkor.ifive.nadab.domain.monthlyreport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "이전 리포트 목록 페이지 응답")
public record AllReportListResponseV2(
        @Schema(description = "현재 페이지의 리포트 목록. 각 item의 type/version/id로 상세 조회 API를 선택합니다.")
        List<AllReportItemResponseV2> items,
        @Schema(description = "조회 조건에 맞는 전체 리포트 개수", example = "43")
        int totalCount,
        @Schema(description = "현재 페이지 번호(1부터 시작)", example = "1")
        int currentPage,
        @Schema(description = "요청한 페이지 크기", example = "7")
        int pageSize,
        @Schema(description = "전체 페이지 수. 조회 결과가 없으면 0입니다.", example = "7")
        int totalPages,
        @Schema(description = "이전 페이지 존재 여부. 이전 버튼 활성화에 사용할 수 있습니다.", example = "false")
        boolean hasPrevious,
        @Schema(description = "다음 페이지 존재 여부. 다음 버튼 활성화에 사용할 수 있습니다.", example = "true")
        boolean hasNext
) {
}
