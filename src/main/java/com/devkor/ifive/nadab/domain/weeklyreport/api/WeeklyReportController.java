package com.devkor.ifive.nadab.domain.weeklyreport.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "주간 리포트 API", description = "주간 리포트 생성 및 조회 관련 API")
@RestController
@RequestMapping("${api_prefix}/weekly-report")
@RequiredArgsConstructor
public class WeeklyReportController {
}
