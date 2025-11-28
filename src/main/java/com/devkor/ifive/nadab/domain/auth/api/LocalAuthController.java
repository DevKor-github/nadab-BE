package com.devkor.ifive.nadab.domain.auth.api;

import com.devkor.ifive.nadab.domain.auth.api.dto.response.LocalTokenResponse;
import com.devkor.ifive.nadab.domain.auth.application.LocalTokenService;
import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import com.devkor.ifive.nadab.global.core.response.ApiResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("local")
@RestController
@RequestMapping("${api_prefix}/local/")
@RequiredArgsConstructor
@Tag(name = "로컬 테스트 API", description = "더미 유저 로그인 API")
public class LocalAuthController {

    private final LocalTokenService localTokenService;

    @GetMapping("/dummy-login")
    @PermitAll
    @Operation(
            summary = "더미 유저 로그인",
            description = """
                    로컬 환경에서 더미 유저로 로그인하여 액세스 토큰을 발급받습니다.
                    """
    )
    public ResponseEntity<ApiResponseDto<LocalTokenResponse>> dummyLogin() {
        LocalTokenResponse tokenResponse = localTokenService.issueDummyAccessToken();

        return ApiResponseEntity.ok(tokenResponse);
    }
}
