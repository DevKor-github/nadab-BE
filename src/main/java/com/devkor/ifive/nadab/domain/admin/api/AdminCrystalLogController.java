package com.devkor.ifive.nadab.domain.admin.api;

import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminCrystalLogResponse;
import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminLogPageResponse;
import com.devkor.ifive.nadab.domain.admin.application.AdminCrystalLogQueryService;
import com.devkor.ifive.nadab.domain.admin.application.AdminLogSearchCondition;
import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import com.devkor.ifive.nadab.global.core.response.ApiResponseEntity;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("/admin/api/logs/crystal")
@RequiredArgsConstructor
public class AdminCrystalLogController {

    private final AdminCrystalLogQueryService adminCrystalLogQueryService;

    @GetMapping
    public ResponseEntity<ApiResponseDto<AdminLogPageResponse<AdminCrystalLogResponse>>> getLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String email
    ) {
        AdminLogSearchCondition condition = AdminLogSearchCondition.of(page, size, nickname, email);
        return ApiResponseEntity.ok(adminCrystalLogQueryService.getLogs(condition));
    }
}
