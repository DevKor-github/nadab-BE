package com.devkor.ifive.nadab.domain.admin.api;

import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminAskChatRagBackfillResponse;
import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminAskChatRagBackfillStatusResponse;
import com.devkor.ifive.nadab.domain.askchat.application.AskChatRagBackfillService;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatRagBackfillResultDto;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatRagBackfillStatusDto;
import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import com.devkor.ifive.nadab.global.core.response.ApiResponseEntity;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("/admin/api/ask-chat/rag")
@RequiredArgsConstructor
public class AdminAskChatRagController {

    private final AskChatRagBackfillService askChatRagBackfillService;

    @GetMapping("/backfill/daily-answers/status")
    public ResponseEntity<ApiResponseDto<AdminAskChatRagBackfillStatusResponse>> getDailyAnswerBackfillStatus() {
        AskChatRagBackfillStatusDto status = askChatRagBackfillService.getCompletedDailyAnswerStatus();
        return ApiResponseEntity.ok(AdminAskChatRagBackfillStatusResponse.from(status));
    }

    @PostMapping("/backfill/daily-answers")
    public ResponseEntity<ApiResponseDto<AdminAskChatRagBackfillResponse>> backfillDailyAnswers() {
        AskChatRagBackfillResultDto result = askChatRagBackfillService.backfillCompletedDailyAnswers();
        return ApiResponseEntity.ok(AdminAskChatRagBackfillResponse.from(result));
    }
}
