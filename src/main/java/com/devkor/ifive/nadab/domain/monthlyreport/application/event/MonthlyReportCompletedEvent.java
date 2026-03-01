package com.devkor.ifive.nadab.domain.monthlyreport.application.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 월간 리포트 완성 이벤트
 * - 월간 리포트가 완성되면 발행
 * - 사용자에게 완성 알림 전송을 위한 트리거
 */
@Getter
@RequiredArgsConstructor
public class MonthlyReportCompletedEvent {

    private final Long reportId;
    private final Long userId;
}