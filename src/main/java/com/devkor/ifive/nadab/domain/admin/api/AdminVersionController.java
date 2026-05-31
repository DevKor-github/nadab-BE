package com.devkor.ifive.nadab.domain.admin.api;

import com.devkor.ifive.nadab.domain.admin.api.dto.request.AdminVersionCreateRequest;
import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminLatestVersionsResponse;
import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminVersionCreateResponse;
import com.devkor.ifive.nadab.domain.admin.application.AdminVersionCommandService;
import com.devkor.ifive.nadab.domain.admin.application.AdminVersionQueryService;
import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import com.devkor.ifive.nadab.global.core.response.ApiResponseEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/api/versions")
@RequiredArgsConstructor
public class AdminVersionController {

    private final AdminVersionQueryService adminVersionQueryService;
    private final AdminVersionCommandService adminVersionCommandService;

    @GetMapping("/latest")
    public ResponseEntity<ApiResponseDto<AdminLatestVersionsResponse>> getLatestVersions() {
        return ApiResponseEntity.ok(adminVersionQueryService.getLatestVersions());
    }

    @PostMapping
    public ResponseEntity<ApiResponseDto<AdminVersionCreateResponse>> createVersion(
            @RequestBody @Valid AdminVersionCreateRequest request
    ) {
        Long appVersionId = adminVersionCommandService.createVersion(request);
        return ApiResponseEntity.created(new AdminVersionCreateResponse(appVersionId));
    }
}
