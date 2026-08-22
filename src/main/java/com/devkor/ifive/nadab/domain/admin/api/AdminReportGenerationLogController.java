package com.devkor.ifive.nadab.domain.admin.api;

import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminLogPageResponse;
import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminReportGenerationLogResponse;
import com.devkor.ifive.nadab.domain.admin.application.AdminLogSearchCondition;
import com.devkor.ifive.nadab.domain.admin.application.AdminReportGenerationLogQueryService;
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
@RequestMapping("/admin/api/logs/report-generation")
@RequiredArgsConstructor
public class AdminReportGenerationLogController {

    private final AdminReportGenerationLogQueryService adminReportGenerationLogQueryService;

    @GetMapping
    public ResponseEntity<ApiResponseDto<AdminLogPageResponse<AdminReportGenerationLogResponse>>> getLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String email
    ) {
        AdminLogSearchCondition condition = AdminLogSearchCondition.of(page, size, nickname, email);
        return ApiResponseEntity.ok(adminReportGenerationLogQueryService.getLogs(condition));
    }
}
