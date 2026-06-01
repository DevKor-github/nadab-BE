package com.devkor.ifive.nadab.domain.admin.api;

import com.devkor.ifive.nadab.domain.admin.api.dto.request.AdminVersionCreateRequest;
import com.devkor.ifive.nadab.domain.admin.api.dto.request.AdminVersionItemUpsertRequest;
import com.devkor.ifive.nadab.domain.admin.api.dto.request.AdminVersionSummaryUpdateRequest;
import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminLatestVersionsResponse;
import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminVersionCreateResponse;
import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminVersionItemCreateResponse;
import com.devkor.ifive.nadab.domain.admin.application.AdminVersionCommandService;
import com.devkor.ifive.nadab.domain.admin.application.AdminVersionItemCommandService;
import com.devkor.ifive.nadab.domain.admin.application.AdminVersionQueryService;
import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import com.devkor.ifive.nadab.global.core.response.ApiResponseEntity;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("/admin/api/versions")
@RequiredArgsConstructor
public class AdminVersionController {

    private final AdminVersionQueryService adminVersionQueryService;
    private final AdminVersionCommandService adminVersionCommandService;
    private final AdminVersionItemCommandService adminVersionItemCommandService;

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

    @PutMapping("/{appVersionId}/summary")
    public ResponseEntity<ApiResponseDto<Void>> updateVersionSummary(
            @PathVariable Long appVersionId,
            @RequestBody @Valid AdminVersionSummaryUpdateRequest request
    ) {
        adminVersionCommandService.updateSummary(appVersionId, request.summary());
        return ApiResponseEntity.noContent();
    }

    @PostMapping("/{appVersionId}/items")
    public ResponseEntity<ApiResponseDto<AdminVersionItemCreateResponse>> createVersionItem(
            @PathVariable Long appVersionId,
            @RequestBody @Valid AdminVersionItemUpsertRequest request
    ) {
        Long appVersionItemId = adminVersionItemCommandService.createItem(appVersionId, request);
        return ApiResponseEntity.created(new AdminVersionItemCreateResponse(appVersionItemId));
    }

    @PutMapping("/items/{appVersionItemId}")
    public ResponseEntity<ApiResponseDto<Void>> updateVersionItem(
            @PathVariable Long appVersionItemId,
            @RequestBody @Valid AdminVersionItemUpsertRequest request
    ) {
        adminVersionItemCommandService.updateItem(appVersionItemId, request);
        return ApiResponseEntity.noContent();
    }

    @DeleteMapping("/items/{appVersionItemId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteVersionItem(
            @PathVariable Long appVersionItemId
    ) {
        adminVersionItemCommandService.deleteItem(appVersionItemId);
        return ApiResponseEntity.noContent();
    }
}
