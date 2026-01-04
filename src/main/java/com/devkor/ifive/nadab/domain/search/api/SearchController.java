package com.devkor.ifive.nadab.domain.search.api;

import com.devkor.ifive.nadab.domain.search.api.dto.response.SearchHistoryListResponse;
import com.devkor.ifive.nadab.domain.search.api.dto.response.SearchHistoryResponse;
import com.devkor.ifive.nadab.domain.search.application.SearchHistoryCommandService;
import com.devkor.ifive.nadab.domain.search.application.SearchHistoryQueryService;
import com.devkor.ifive.nadab.domain.search.core.entity.SearchHistory;
import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import com.devkor.ifive.nadab.global.core.response.ApiResponseEntity;
import com.devkor.ifive.nadab.global.security.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "검색 API", description = "검색어 히스토리 관리 API")
@RestController
@RequestMapping("${api_prefix}/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchHistoryQueryService searchHistoryQueryService;
    private final SearchHistoryCommandService searchHistoryCommandService;

    @GetMapping("/histories")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "최근 검색어 조회",
            description = "최근 검색어 10개를 조회합니다. 응답 리스트는 위에서부터 최신순입니다. (id는 순서와 무관)",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = SearchHistoryListResponse.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<SearchHistoryListResponse>> getRecentSearches(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<SearchHistory> histories = searchHistoryQueryService.getRecentSearches(principal.getId());
        SearchHistoryListResponse response = toListResponse(histories);
        return ApiResponseEntity.ok(response);
    }

    @DeleteMapping("/histories/{historyId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "검색어 개별 삭제",
            description = "검색어를 삭제한 후 최신 10개를 반환합니다. 응답 리스트는 위에서부터 최신순입니다. (id는 순서와 무관)",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "삭제 성공",
                            content = @Content(schema = @Schema(implementation = SearchHistoryListResponse.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "본인의 검색어가 아님", content = @Content),
                    @ApiResponse(responseCode = "404", description = "검색어를 찾을 수 없음", content = @Content)

            }
    )
    public ResponseEntity<ApiResponseDto<SearchHistoryListResponse>> deleteSearchHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long historyId
    ) {
        searchHistoryCommandService.deleteSearchHistory(principal.getId(), historyId);

        // 삭제 후 최신 10개 조회
        List<SearchHistory> histories = searchHistoryQueryService.getRecentSearches(principal.getId());
        SearchHistoryListResponse response = toListResponse(histories);
        return ApiResponseEntity.ok(response);
    }

    @DeleteMapping("/histories")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "전체 검색어 삭제",
            description = "모든 검색어를 삭제합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<Void>> deleteAllSearchHistories(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        searchHistoryCommandService.deleteAllSearchHistories(principal.getId());
        return ApiResponseEntity.noContent();
    }

    /**
     * Entity → Response 변환 메서드
     */
    private SearchHistoryListResponse toListResponse(List<SearchHistory> histories) {
        List<SearchHistoryResponse> items = histories.stream()
                .map(this::toResponse)
                .toList();
        return new SearchHistoryListResponse(items);
    }

    private SearchHistoryResponse toResponse(SearchHistory history) {
        return new SearchHistoryResponse(
                history.getId(),
                history.getKeyword()
        );
    }
}