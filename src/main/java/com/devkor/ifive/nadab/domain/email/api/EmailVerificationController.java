package com.devkor.ifive.nadab.domain.email.api;

import com.devkor.ifive.nadab.domain.email.api.dto.request.SendVerificationCodeRequest;
import com.devkor.ifive.nadab.domain.email.api.dto.request.VerifyCodeRequest;
import com.devkor.ifive.nadab.domain.email.application.EmailVerificationCommandService;
import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import com.devkor.ifive.nadab.global.core.response.ApiResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 이메일 인증 API
 * - 회원가입, 비밀번호 재설정 시 이메일 인증 코드 발송 및 검증
 */
@Tag(name = "이메일 인증 API", description = "이메일 인증 코드 발송 및 검증 API")
@RestController
@RequestMapping("${api_prefix}/email")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationCommandService emailVerificationCommandService;

    @PostMapping("/code")
    @PermitAll
    @Operation(
            summary = "이메일 인증 코드 발송",
            description = """
                    회원가입 또는 비밀번호 재설정을 위한 이메일 인증 코드를 발송합니다.<br>
                    6자리 숫자 인증 코드가 이메일로 전송되며, 유효기간은 3분입니다.<br>
                    동일한 이메일과 인증 타입으로 재요청 시, 기존 인증 코드는 제거되고 새로운 코드가 생성됩니다.<br>
                    <br>
                    <b>주의:</b> 이메일 발송은 비동기로 처리되며, SMTP 실패 시에도 200 응답이 반환됩니다.
                    이메일을 받지 못한 경우 동일한 API를 재호출하여 새로운 인증 코드를 발급받을 수 있습니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "인증 코드 발송 성공",
                            content = @Content(schema = @Schema(implementation = ApiResponseDto.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "이메일 형식 오류, 인증 타입 누락",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<Void>> sendVerificationCode(
            @Valid @RequestBody SendVerificationCodeRequest request
    ) {
        emailVerificationCommandService.sendVerificationCode(request.email(), request.verificationType());
        return ApiResponseEntity.ok("인증 코드가 발송되었습니다");
    }

    @PostMapping("/code/verification")
    @PermitAll
    @Operation(
            summary = "이메일 인증 코드 검증",
            description = """
                    이메일로 받은 6자리 인증 코드를 검증합니다.<br>
                    인증 성공 시, 이후 회원가입 또는 비밀번호 재설정을 진행할 수 있습니다.<br>
                    인증 코드는 3분 후 자동 만료되며, 만료된 코드는 재발송이 필요합니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "인증 코드 검증 성공",
                            content = @Content(schema = @Schema(implementation = ApiResponseDto.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "이메일/인증 코드 형식 오류, 코드 불일치, 만료된 코드, 이미 인증 완료",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "인증 요청을 찾을 수 없음 - 발송 이력이 없거나 코드가 존재하지 않는 경우",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<Void>> verifyCode(
            @Valid @RequestBody VerifyCodeRequest request
    ) {
        emailVerificationCommandService.verifyCode(request.email(), request.code(), request.verificationType());
        return ApiResponseEntity.ok("이메일 인증이 완료되었습니다");
    }
}