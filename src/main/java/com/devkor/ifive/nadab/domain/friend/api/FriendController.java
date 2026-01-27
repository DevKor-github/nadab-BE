package com.devkor.ifive.nadab.domain.friend.api;

import com.devkor.ifive.nadab.domain.friend.api.dto.request.CreateFriendshipRequest;
import com.devkor.ifive.nadab.domain.friend.api.dto.response.*;
import com.devkor.ifive.nadab.domain.friend.application.FriendshipCommandService;
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

@Tag(name = "친구 관리 API", description = "친구 요청 및 관리 API")
@RestController
@RequestMapping("${api_prefix}/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendshipCommandService friendshipCommandService;
    private final FriendshipQueryService friendshipQueryService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "친구 목록 조회",
            description = """
                    친구 목록을 조회합니다.

                    - totalCount: 총 친구 수
                    - friends: 친구 목록 (최신순)
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = FriendListResponse.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<FriendListResponse>> getFriends(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        FriendListResponse response = friendshipQueryService.getFriends(principal.getId());
        return ApiResponseEntity.ok(response);
    }

    @GetMapping("/requests")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "받은 친구 요청 목록 조회",
            description = """
                    받은 친구 요청 목록을 조회합니다.

                    - totalCount: 총 요청 수
                    - requests: 전체 요청 목록 (최신순)
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = PendingFriendListResponse.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<PendingFriendListResponse>> getReceivedRequests(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        PendingFriendListResponse response = friendshipQueryService.getReceivedRequests(principal.getId());
        return ApiResponseEntity.ok(response);
    }

    @PostMapping("/requests")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "친구 요청 보내기",
            description = """
                    닉네임으로 유저를 찾아 친구 요청을 보냅니다.

                    - 본인에게는 요청 불가
                    - 이미 친구이거나 요청이 존재하면 불가
                    - 발신자/수신자 모두 친구 수 20명 미만이어야 함
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "요청 성공",
                            content = @Content(schema = @Schema(implementation = FriendshipResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = """
                                    - ErrorCode: VALIDATION_FAILED - 닉네임 누락 또는 길이 초과
                                    - ErrorCode: FRIENDSHIP_CANNOT_SEND_TO_SELF - 본인에게 요청 불가
                                    - ErrorCode: FRIENDSHIP_ALREADY_EXISTS - 이미 친구이거나 요청 존재
                                    - ErrorCode: FRIEND_LIMIT_EXCEEDED - 친구 수 20명 초과 (본인)
                                    - ErrorCode: FRIEND_RECEIVER_LIMIT_EXCEEDED - 친구 수 20명 초과 (상대방)
                                    """,
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "ErrorCode: USER_NOT_FOUND - 수신자를 찾을 수 없음",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<FriendshipResponse>> sendFriendRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateFriendshipRequest request
    ) {
        Long friendshipId = friendshipCommandService.sendFriendRequest(
                principal.getId(),
                request.receiverNickname()
        );
        FriendshipResponse response = new FriendshipResponse(friendshipId);
        return ApiResponseEntity.ok(response);
    }

    @DeleteMapping("/requests/{friendshipId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "친구 요청 취소",
            description = "본인이 보낸 친구 요청을 취소합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "취소 성공"),
                    @ApiResponse(
                            responseCode = "400",
                            description = "ErrorCode: FRIENDSHIP_ALREADY_PROCESSED - 이미 처리된 요청",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "ErrorCode: FRIENDSHIP_ACCESS_FORBIDDEN - 본인이 보낸 요청이 아님",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "ErrorCode: FRIENDSHIP_NOT_FOUND - 요청을 찾을 수 없음",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<Void>> cancelFriendRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long friendshipId
    ) {
        friendshipCommandService.cancelFriendRequest(principal.getId(), friendshipId);
        return ApiResponseEntity.noContent();
    }

    @PostMapping("/requests/{friendshipId}/accept")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "친구 요청 수락",
            description = """
                    받은 친구 요청을 수락합니다.

                    - 양쪽 모두 친구 수 20명 미만이어야 함
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "수락 성공"),
                    @ApiResponse(
                            responseCode = "400",
                            description = """
                                    - ErrorCode: FRIENDSHIP_ALREADY_PROCESSED - 이미 처리된 요청
                                    - ErrorCode: FRIEND_LIMIT_EXCEEDED - 친구 수 20명 초과
                                    """,
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "ErrorCode: FRIENDSHIP_ACCESS_FORBIDDEN - 본인에게 온 요청이 아님",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "ErrorCode: FRIENDSHIP_NOT_FOUND - 요청을 찾을 수 없음",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<Void>> acceptFriendRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long friendshipId
    ) {
        friendshipCommandService.acceptFriendRequest(principal.getId(), friendshipId);
        return ApiResponseEntity.noContent();
    }

    @PostMapping("/requests/{friendshipId}/reject")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "친구 요청 거절",
            description = "받은 친구 요청을 거절합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "거절 성공"),
                    @ApiResponse(
                            responseCode = "400",
                            description = "ErrorCode: FRIENDSHIP_ALREADY_PROCESSED - 이미 처리된 요청",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "ErrorCode: FRIENDSHIP_ACCESS_FORBIDDEN - 본인에게 온 요청이 아님",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "ErrorCode: FRIENDSHIP_NOT_FOUND - 요청을 찾을 수 없음",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<Void>> rejectFriendRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long friendshipId
    ) {
        friendshipCommandService.rejectFriendRequest(principal.getId(), friendshipId);
        return ApiResponseEntity.noContent();
    }

    @DeleteMapping("/{friendshipId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "친구 삭제",
            description = "친구를 삭제합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "삭제 성공"),
                    @ApiResponse(
                            responseCode = "400",
                            description = "ErrorCode: FRIENDSHIP_ALREADY_PROCESSED - 이미 처리된 관계",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "ErrorCode: FRIENDSHIP_ACCESS_FORBIDDEN - 본인의 친구가 아님",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "ErrorCode: FRIENDSHIP_NOT_FOUND - 친구 관계를 찾을 수 없음",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<Void>> deleteFriend(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long friendshipId
    ) {
        friendshipCommandService.deleteFriend(principal.getId(), friendshipId);
        return ApiResponseEntity.noContent();
    }
}