package com.devkor.ifive.nadab.domain.user.api;

import com.devkor.ifive.nadab.domain.user.api.dto.request.CreateProfileImageUploadUrlRequest;
import com.devkor.ifive.nadab.domain.user.api.dto.request.UpdateUserInterestRequest;
import com.devkor.ifive.nadab.domain.user.api.dto.request.UpdateUserProfileRequest;
import com.devkor.ifive.nadab.domain.user.api.dto.response.CheckNicknameResponse;
import com.devkor.ifive.nadab.domain.user.api.dto.response.CreateProfileImageUploadUrlResponse;
import com.devkor.ifive.nadab.domain.user.api.dto.response.UpdateUserProfileResponse;
import com.devkor.ifive.nadab.domain.user.api.dto.response.UserProfileResponse;
import com.devkor.ifive.nadab.domain.user.application.UserCommandService;
import com.devkor.ifive.nadab.domain.user.application.UserQueryService;
import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import com.devkor.ifive.nadab.global.core.response.ApiResponseEntity;
import com.devkor.ifive.nadab.global.security.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "유저 API", description = "유저 관련 API")
@RestController
@RequestMapping("${api_prefix}/user")
@RequiredArgsConstructor
public class UserController {

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "나의 프로필 조회",
            description = "로그인한 사용자의 프로필 정보를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(schema = @Schema(implementation = UserProfileResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = """
                                    잘못된 요청
                                    - ErrorCode: USER_NOT_FOUND - 사용자를 찾을 수 없음
                                    - ErrorCode: USER_INTEREST_NOT_FOUND - 관심 주제를 찾을 수 없음
                                    """,
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패 (JWT 토큰 관련)",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        UserProfileResponse profile = userQueryService.getUserProfile(principal.getId());
        return ApiResponseEntity.ok(profile);
    }

    @PostMapping("/me/profile-image/upload-url")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "프로필 이미지 업로드 PresignedURL 생성",
            description = """
                    프로필 이미지를 업로드할 수 있는 PresignedURL을 생성합니다.
                    
                    - HTTP Method: PUT
                    - Headers:
                        - Content-Type(필수): image/jpeg, image/png만 허용
                    - Body: 이미지 파일
                    - URL 만료 시간: 5분
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(schema = @Schema(implementation = CreateProfileImageUploadUrlResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = """
                                    잘못된 요청
                                    - ErrorCode: IMAGE_UNSUPPORTED_TYPE - 지원하지 않는 이미지 타입
                                    """,
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패 (JWT 토큰 관련)",
                            content = @Content
                    )
            }

    )
    public ResponseEntity<ApiResponseDto<CreateProfileImageUploadUrlResponse>> createProfileImageUploadUrl(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateProfileImageUploadUrlRequest request) {
        CreateProfileImageUploadUrlResponse response =
                userCommandService.createUploadUrl(principal.getId(), request);
        return ApiResponseEntity.ok(response);
    }

    @DeleteMapping("/me/profile-image")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "프로필 이미지 삭제",
            description = "사용자의 프로필 이미지를 삭제합니다. 기본 프로필 이미지로 변경됩니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "프로필 이미지 삭제 성공",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = """
                                    잘못된 요청
                                    - ErrorCode: USER_NOT_FOUND - 사용자를 찾을 수 없음
                                    """,
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패 (JWT 토큰 관련)",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<Void>> deleteProfileImage(
            @AuthenticationPrincipal UserPrincipal principal) {
        userCommandService.deleteProfileImage(principal.getId());
        return ApiResponseEntity.noContent();
    }

    @PatchMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "나의 프로필 수정",
            description = """
                    로그인한 사용자의 프로필 정보를 수정합니다.
                    닉네임과 프로필 이미지를 하나씩만, 또는 둘 다 수정할 수 있습니다.
                    **적어도 하나의 필드를 포함해야 합니다.**
                    **5MB 이하의 이미지 파일만 허용됩니다.**
                    
                    (예) 프로필 이미지만 수정 시 nickname은 null
                   
                    프로필 이미지 수정의 경우,
                    POST /user/me/profile-image/upload-url 엔드포인트로
                    미리 발급받은 PresignedURL을 통해 이미지를 업로드한 후,
                    해당 엔드포인트에서 반환된 objectKey를 이 요청에 포함시켜야 합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "프로필 수정 성공",
                            content = @Content(schema = @Schema(implementation = UpdateUserProfileResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = """
                                    잘못된 요청
                                    - ErrorCode: USER_NOT_FOUND - 사용자를 찾을 수 없음
                                    - ErrorCode: USER_UPDATE_NO_DATA - 수정할 프로필 정보가 없음
                                    - ErrorCode: IMAGE_SIZE_EXCEEDED - 이미지 크기 제한 초과 (5MB)
                                    - ErrorCode: IMAGE_UNSUPPORTED_TYPE - 지원하지 않는 이미지 타입
                                    - ErrorCode: IMAGE_METADATA_INVALID - 파일 메타데이터를 읽을 수 없음
                                    """,
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패 (JWT 토큰 관련)",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<UpdateUserProfileResponse>> updateUserProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateUserProfileRequest request) {
        UpdateUserProfileResponse response =
                userCommandService.updateUserProfile(principal.getId(), request);
        return ApiResponseEntity.ok(response);
    }

    @GetMapping("/check-nickname")
    @PermitAll
    @Operation(
            summary = "닉네임 사용 가능 여부 조회",
            description = """
            닉네임이 사용 가능한지 종합적으로 검사합니다.
            - 닉네임은 2자 이상 10자 이하이어야 합니다.
            - 한글과 영어 대소문자만 허용됩니다.
            - 닉네임은 공백으로 시작하거나 끝날 수 없습니다.
            - 이미 사용 중인 닉네임은 사용할 수 없습니다.
            - 예약어(admin, root 등)는 사용할 수 없습니다.
            - 비속어 및 부적절한 단어가 포함된 닉네임은 사용할 수 없습니다.
            - 로그인 상태에서 자신의 현재 닉네임을 보내면 사용 불가로 처리됩니다.
            """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공 - 사용 가능 여부는 응답 내용으로 판단",
                            content = @Content(schema = @Schema(implementation = CheckNicknameResponse.class), mediaType = "application/json")
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<CheckNicknameResponse>> checkNickname(
            @RequestParam String nickname) {
        CheckNicknameResponse response = userQueryService.checkNickname(nickname);
        return ApiResponseEntity.ok(response);
    }

    @PatchMapping("/interest")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "유저 관심 주제 업데이트",
            description = """
                    유저의 관심 주제를 업데이트합니다. 하나만 선택 가능합니다. 온보딩 시에도 사용됩니다.
                    
                    선택 가능한 관심 주제 코드는 다음과 같습니다.
                    
                    - **PREFERENCE** : 취향
                    - **EMOTION** : 감정
                    - **ROUTINE** : 루틴
                    - **RELATIONSHIP** : 인간관계
                    - **LOVE** : 사랑
                    - **VALUES** : 가치관
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "관심 주제 업데이트 성공",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = """
                                    잘못된 요청
                                    - ErrorCode: USER_NOT_FOUND - 사용자를 찾을 수 없음
                                    - ErrorCode: USER_INTEREST_NOT_FOUND - 관심 주제를 찾을 수 없음
                                    """,
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패 (JWT 토큰 관련)",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<Void>> updateUserInterests(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateUserInterestRequest request) {
        userCommandService.updateUserInterest(principal.getId(), request);
        return ApiResponseEntity.noContent();
    }
}
