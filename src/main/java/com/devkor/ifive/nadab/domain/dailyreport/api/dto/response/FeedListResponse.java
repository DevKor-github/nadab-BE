package com.devkor.ifive.nadab.domain.dailyreport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "피드 목록 응답")
public record FeedListResponse(

        @Schema(description = "피드 목록")
        List<FeedResponse> feeds
) {
}
