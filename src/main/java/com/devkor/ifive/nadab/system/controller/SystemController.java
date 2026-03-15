package com.devkor.ifive.nadab.system.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@Tag(name = "System API", description = "시스템 상태 점검 관련 API")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class SystemController {

    @GetMapping("/health")
    @PermitAll
    @Operation(
            summary = "시스템 헬스 체크",
            description = "서비스의 기본 동작 여부를 확인하기 위한 헬스 체크 엔드포인트입니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "정상 응답")
            }
    )
    public String ping() {
        return "Nadab is running.";
    }
}
