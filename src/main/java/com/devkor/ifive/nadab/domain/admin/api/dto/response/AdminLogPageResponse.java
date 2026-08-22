package com.devkor.ifive.nadab.domain.admin.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "관리자 로그 페이지 응답")
public record AdminLogPageResponse<T>(
        @Schema(description = "현재 페이지의 로그 목록")
        List<T> items,

        @Schema(description = "전체 로그 개수", example = "43")
        long totalCount,

        @Schema(description = "현재 페이지 번호(1부터 시작)", example = "1")
        int currentPage,

        @Schema(description = "요청한 페이지 크기", example = "20")
        int pageSize,

        @Schema(description = "전체 페이지 수", example = "3")
        int totalPages,

        @Schema(description = "이전 페이지 존재 여부", example = "false")
        boolean hasPrevious,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {

    public static <T> AdminLogPageResponse<T> from(Page<T> page) {
        int currentPage = page.getNumber() + 1;
        int totalPages = page.getTotalPages();

        return new AdminLogPageResponse<>(
                page.getContent(),
                page.getTotalElements(),
                currentPage,
                page.getSize(),
                totalPages,
                currentPage > 1 && totalPages > 0,
                totalPages > 0 && currentPage < totalPages
        );
    }
}
