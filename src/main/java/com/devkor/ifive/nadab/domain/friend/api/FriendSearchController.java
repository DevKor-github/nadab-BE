package com.devkor.ifive.nadab.domain.friend.api;

import com.devkor.ifive.nadab.domain.friend.api.dto.request.SaveFriendSearchRequest;
import com.devkor.ifive.nadab.domain.friend.api.dto.request.SearchUserRequest;
import com.devkor.ifive.nadab.domain.friend.api.dto.response.SearchHistoryListResponse;
import com.devkor.ifive.nadab.domain.friend.api.dto.response.SearchUserListResponse;
import com.devkor.ifive.nadab.domain.friend.application.FriendSearchHistoryCommandService;
import com.devkor.ifive.nadab.domain.friend.application.FriendSearchHistoryQueryService;
import com.devkor.ifive.nadab.domain.friend.application.FriendshipQueryService;
import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import com.devkor.ifive.nadab.global.core.response.ApiResponseEntity;
import com.devkor.ifive.nadab.global.security.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "친구 검색 API", description = "유저 검색 및 검색 기록 관리 API")
@RestController
@RequestMapping("${api_prefix}/friends/search")
@RequiredArgsConstructor
public class FriendSearchController {

    private final FriendshipQueryService friendshipQueryService;
    private final FriendSearchHistoryCommandService friendSearchHistoryCommandService;
    private final FriendSearchHistoryQueryService friendSearchHistoryQueryService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "유저 검색",
            description = """
                    닉네임으로 유저를 검색합니다. (Cursor-based Pagination)

                    ### 검색 모드
                    - 닉네임 부분 매칭: 검색어가 닉네임의 앞, 중간, 끝 어디에 있어도 검색됨
                      * 예: "모래" 검색 시 → "모래", "모래사장", "나모래다", "예쁜모래" 모두 검색
                    - 정렬: 관련도 순
                      * 1순위: 완전 일치 (닉네임 = 검색어)
                      * 2순위: 시작 매칭 (닉네임이 검색어로 시작)
                      * 3순위: 부분 매칭 (그 외)
                      * 같은 순위 내에서는 닉네임 가나다순 정렬
                    - 페이지 크기: 30개 고정
                    - 제외 대상: 탈퇴한 유저는 검색 결과에서 제외됨

                    ### Request Parameters
                    - keyword (필수): 검색 키워드 (닉네임, 1~255자)
                    - cursor (선택): 이전 응답의 nextCursor 값을 그대로 사용 (첫 페이지 요청 시에는 생략)

                    ### Response
                    - pendingRequests: 나한테 친구 요청 보낸 사람들 중 검색어와 일치하는 사람들
                      * 첫 페이지 (cursor=null): 요청 보낸 사람들 포함 (탈퇴한 유저 제외)
                      * 이후 페이지 (cursor 있음): 빈 배열 반환
                      * 프론트엔드에서는 첫 페이지에서 받은 값을 계속 유지하면 됩니다.
                    - searchResults: 검색 결과 리스트 (항상 정확히 30개, 본인 포함 가능)
                      * 제외 대상: 받은 친구 요청 유저 (pendingRequests에 표시), 탈퇴한 유저
                      * 본인이 검색되면 relationshipStatus는 SELF로 표시됨
                      * 중복 없음: 받은 요청 유저는 pendingRequests에만 표시되고 searchResults에는 포함 안 됨
                    - nextCursor: 다음 페이지 요청 시 사용할 커서 (마지막 페이지면 null)
                    - hasNext: 다음 페이지 존재 여부

                    ### 무한 스크롤 구현 (쿼리 파라미터 형식)
                    1. 첫 페이지: GET /api/v1/friends/search?keyword=모래
                       → pendingRequests: [친구 요청 보낸 사람들], searchResults: [검색 결과 30개]
                    2. 다음 페이지: GET /api/v1/friends/search?keyword=모래&cursor=모래가나
                       → pendingRequests: [], searchResults: [다음 30개]
                       (response의 nextCursor 값을 cursor 파라미터로 전달)
                    3. hasNext=false가 될 때까지 반복
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "검색 성공",
                            content = @Content(schema = @Schema(implementation = SearchUserListResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "ErrorCode: VALIDATION_FAILED - 검색 키워드 누락 또는 길이 초과",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<SearchUserListResponse>> searchUsers(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @ModelAttribute SearchUserRequest request
    ) {
        SearchUserListResponse response = friendshipQueryService.searchUsers(
                principal.getId(),
                request.keyword(),
                request.cursor()
        );
        return ApiResponseEntity.ok(response);
    }

    @PostMapping("/histories")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "친구 검색 기록 저장",
            description = """
                    사용자가 친구 검색 결과를 클릭했을 때 검색 기록을 저장합니다.

                    - 프로필을 조회했을 때 호출됩니다.
                    - 이미 존재하는 검색 기록은 최신 순서로 갱신됩니다.
                    - 최대 100개까지 저장되며, 초과 시 오래된 것부터 삭제됩니다.

                    ### 에러 처리
                    - DB 장애 등으로 내부 오류가 발생해서 실제 저장이 실패하더라도 204를 반환합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "저장 완료 (내부 오류 발생 시에도 204 반환)"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "ErrorCode: VALIDATION_FAILED - 잘못된 요청 (닉네임 누락 또는 길이 초과)",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<Void>> saveSearchHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SaveFriendSearchRequest request
    ) {
        friendSearchHistoryCommandService.saveSearchHistoryByNickname(principal.getId(), request.nickname());
        return ApiResponseEntity.noContent();
    }

    @GetMapping("/histories")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "최근 검색어 조회",
            description = "최근 검색한 유저 5명을 조회합니다. 응답 리스트는 최신순입니다.",
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
        SearchHistoryListResponse response = friendSearchHistoryQueryService.getRecentSearches(principal.getId());
        return ApiResponseEntity.ok(response);
    }

    @DeleteMapping("/histories/{nickname}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "검색어 개별 삭제",
            description = "특정 닉네임의 검색 기록을 삭제하고, 삭제 후 남은 최신 검색 기록 5개를 반환합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "삭제 성공 및 남은 검색 기록 반환",
                            content = @Content(schema = @Schema(implementation = SearchHistoryListResponse.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<SearchHistoryListResponse>> deleteSearchHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String nickname
    ) {
        friendSearchHistoryCommandService.deleteSearchHistoryByNickname(principal.getId(), nickname);

        // 삭제 후 최신 검색 기록 5개 조회
        SearchHistoryListResponse response = friendSearchHistoryQueryService.getRecentSearches(principal.getId());

        return ApiResponseEntity.ok(response);
    }

    @DeleteMapping("/histories")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "전체 검색어 삭제",
            description = "모든 검색 기록을 삭제합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<Void>> deleteAllSearchHistories(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        friendSearchHistoryCommandService.deleteAllSearchHistories(principal.getId());
        return ApiResponseEntity.noContent();
    }
}