package com.devkor.ifive.nadab.domain.stats.application;

import com.devkor.ifive.nadab.domain.auth.core.entity.WithdrawalReasonType;
import com.devkor.ifive.nadab.domain.stats.core.dto.withdrawal.WithdrawalEventRowViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.withdrawal.WithdrawalStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.repository.WithdrawalStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WithdrawalStatsService {

    private static final int RECENT_WITHDRAWAL_EVENT_LIMIT = 100;
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final WithdrawalStatsRepository repo;

    public WithdrawalStatsViewModel getWithdrawalStats() {
        List<Object[]> rows = repo.findLatestWithdrawalReasonRows(RECENT_WITHDRAWAL_EVENT_LIMIT);
        List<Object[]> totalReasonRows = repo.countAllWithdrawalReasons();

        Map<EventKey, EventAccumulator> eventMap = new LinkedHashMap<>();
        Map<WithdrawalReasonType, Long> reasonCountMap = new EnumMap<>(WithdrawalReasonType.class);

        for (Object[] row : rows) {
            long userId = toLong(row[0]);
            OffsetDateTime withdrawnAt = toOffsetDateTime(row[1]);
            String reasonCode = String.valueOf(row[2]);
            WithdrawalReasonType reasonType = parseReasonType(reasonCode);
            String customReason = row[3] == null ? null : String.valueOf(row[3]).trim();

            OffsetDateTime normalizedWithdrawnAt = withdrawnAt.truncatedTo(ChronoUnit.SECONDS);
            EventKey key = new EventKey(userId, normalizedWithdrawnAt);
            EventAccumulator accumulator = eventMap.computeIfAbsent(
                    key,
                    k -> new EventAccumulator(formatDateTime(normalizedWithdrawnAt))
            );

            accumulator.reasons.add(toReasonLabel(reasonType, reasonCode));
            if (customReason != null && !customReason.isEmpty()) {
                accumulator.customReason = customReason;
            }

        }

        for (Object[] row : totalReasonRows) {
            String reasonCode = String.valueOf(row[0]);
            WithdrawalReasonType reasonType = parseReasonType(reasonCode);
            if (reasonType == null) {
                continue;
            }
            reasonCountMap.put(reasonType, toLong(row[1]));
        }

        List<WithdrawalReasonType> reasonTypes = Arrays.stream(WithdrawalReasonType.values()).toList();
        List<String> reasonLabels = reasonTypes.stream()
                .map(this::toReasonLabel)
                .toList();
        List<Long> reasonCounts = reasonTypes.stream()
                .map(type -> reasonCountMap.getOrDefault(type, 0L))
                .toList();

        List<WithdrawalEventRowViewModel> eventRows = eventMap.values().stream()
                .map(event -> new WithdrawalEventRowViewModel(
                        event.withdrawnAt,
                        String.join(", ", event.reasons),
                        event.customReason == null ? "-" : event.customReason
                ))
                .toList();

        return new WithdrawalStatsViewModel(
                RECENT_WITHDRAWAL_EVENT_LIMIT,
                eventRows.size(),
                reasonLabels,
                reasonCounts,
                eventRows,
                OffsetDateTime.now(SEOUL).format(FMT)
        );
    }

    private long toLong(Object value) {
        if (value instanceof Number n) {
            return n.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private OffsetDateTime toOffsetDateTime(Object value) {
        if (value instanceof OffsetDateTime odt) {
            return odt;
        }
        if (value instanceof LocalDateTime ldt) {
            return ldt.atZone(SEOUL).toOffsetDateTime();
        }
        if (value instanceof Timestamp ts) {
            return ts.toInstant().atZone(SEOUL).toOffsetDateTime();
        }
        return OffsetDateTime.parse(String.valueOf(value));
    }

    private String formatDateTime(OffsetDateTime value) {
        return value.atZoneSameInstant(SEOUL).format(FMT);
    }

    private WithdrawalReasonType parseReasonType(String reasonCode) {
        try {
            return WithdrawalReasonType.valueOf(reasonCode);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String toReasonLabel(WithdrawalReasonType reasonType) {
        return switch (reasonType) {
            case DAILY_LOGGING_BURDEN -> "매일 기록이 부담";
            case INSUFFICIENT_QUESTION_ANALYSIS -> "질문·분석 부족";
            case LOSS_OF_INTEREST_IN_WRITING -> "흥미 상실";
            case PRIVACY_RECORD_CONCERN -> "기록 보안 우려";
            case APP_ERROR_OR_SLOWNESS -> "오류·속도 문제";
            case OTHER -> "기타(직접 입력)";
        };
    }

    private String toReasonLabel(WithdrawalReasonType reasonType, String rawReasonCode) {
        if (reasonType == null) {
            return rawReasonCode;
        }
        return toReasonLabel(reasonType);
    }

    private record EventKey(long userId, OffsetDateTime withdrawnAt) {
    }

    private static class EventAccumulator {
        private final String withdrawnAt;
        private final List<String> reasons = new ArrayList<>();
        private String customReason;

        private EventAccumulator(String withdrawnAt) {
            this.withdrawnAt = withdrawnAt;
        }
    }
}
