package com.devkor.ifive.nadab.domain.admin.api;

import com.devkor.ifive.nadab.domain.admin.api.dto.request.AdminLoginRequest;
import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminAuthStatusResponse;
import com.devkor.ifive.nadab.domain.admin.application.AdminPageAuthCommandService;
import com.devkor.ifive.nadab.domain.admin.infra.security.AdminPageAuthCookieService;
import com.devkor.ifive.nadab.domain.admin.infra.security.AdminPageAuthTokenService;
import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import com.devkor.ifive.nadab.global.core.response.ApiResponseEntity;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/api")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminPageAuthCommandService adminPageAuthCommandService;
    private final AdminPageAuthTokenService adminPageAuthTokenService;
    private final AdminPageAuthCookieService adminPageAuthCookieService;

    @PostMapping("/login")
    @PermitAll
    public ResponseEntity<ApiResponseDto<Void>> login(
            @RequestBody @Valid AdminLoginRequest request,
            HttpServletResponse response
    ) {
        adminPageAuthCommandService.validatePassword(request.password());
        String token = adminPageAuthTokenService.issueToken();
        adminPageAuthCookieService.addCookie(response, token);
        return ApiResponseEntity.noContent();
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDto<Void>> logout(HttpServletResponse response) {
        adminPageAuthCookieService.expireCookie(response);
        return ApiResponseEntity.noContent();
    }

    @GetMapping("/auth-status")
    public ResponseEntity<ApiResponseDto<AdminAuthStatusResponse>> authStatus() {
        return ApiResponseEntity.ok(new AdminAuthStatusResponse(true));
    }
}
