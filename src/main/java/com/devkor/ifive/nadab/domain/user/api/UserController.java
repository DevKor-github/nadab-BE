package com.devkor.ifive.nadab.domain.user.api;

import com.devkor.ifive.nadab.domain.user.api.dto.request.CreateProfileImageUploadUrlRequest;
import com.devkor.ifive.nadab.domain.user.api.dto.response.CreateProfileImageUploadUrlResponse;
import com.devkor.ifive.nadab.domain.user.api.dto.response.UserProfileResponse;
import com.devkor.ifive.nadab.domain.user.core.service.ProfileImageService;
import com.devkor.ifive.nadab.domain.user.application.UserCommandService;
import com.devkor.ifive.nadab.domain.user.application.UserQueryService;
import com.devkor.ifive.nadab.domain.user.core.service.UserProfileUpdateService;
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

@Tag(name = "유저 API", description = "유저 관련 API")
@RestController
@RequestMapping("${api_prefix}/user")
@RequiredArgsConstructor
public class UserController {

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;
    private final ProfileImageService profileImageService;
    private final UserProfileUpdateService userProfileUpdateService;

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
                            responseCode = "401",
                            description = "인증 실패",
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
                            description = "잘못된 요청 (예: 지원하지 않는 확장자)",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content
                    )
            }

    )
    public ResponseEntity<ApiResponseDto<CreateProfileImageUploadUrlResponse>> createProfileImageUploadUrl(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateProfileImageUploadUrlRequest request) {
        CreateProfileImageUploadUrlResponse response =
                profileImageService.createUploadUrl(principal.getId(), request);
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
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<Void>> deleteProfileImage(
            @AuthenticationPrincipal UserPrincipal principal) {
        userProfileUpdateService.deleteProfileImage(principal.getId());
        return ApiResponseEntity.noContent();
    }

}
