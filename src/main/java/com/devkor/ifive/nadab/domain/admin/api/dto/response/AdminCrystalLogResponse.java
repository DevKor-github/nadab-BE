package com.devkor.ifive.nadab.domain.admin.api.dto.response;

import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLog;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogReason;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "관리자 크리스탈 로그")
public record AdminCrystalLogResponse(
        @Schema(description = "로그 ID", example = "1001")
        Long id,

        @Schema(description = "로그 사용자")
        AdminLogUserResponse user,

        @Schema(description = "크리스탈 변동량", example = "-100")
        long delta,

        @Schema(description = "변동 후 크리스탈 잔액", example = "900")
        long balanceAfter,

        @Schema(description = "변동 사유", example = "REPORT_GENERATE_MONTHLY")
        CrystalLogReason reason,

        @Schema(description = "로그 상태", example = "CONFIRMED")
        CrystalLogStatus status,

        @Schema(description = "참조 유형", example = "MONTHLY_REPORT")
        String refType,

        @Schema(description = "참조 ID", example = "2001")
        Long refId,

        @Schema(description = "로그 생성 시각")
        OffsetDateTime createdAt
) {

    public static AdminCrystalLogResponse from(CrystalLog log) {
        return new AdminCrystalLogResponse(
                log.getId(),
                AdminLogUserResponse.from(log.getUser()),
                log.getDelta(),
                log.getBalanceAfter(),
                log.getReason(),
                log.getStatus(),
                log.getRefType(),
                log.getRefId(),
                log.getCreatedAt()
        );
    }
}
