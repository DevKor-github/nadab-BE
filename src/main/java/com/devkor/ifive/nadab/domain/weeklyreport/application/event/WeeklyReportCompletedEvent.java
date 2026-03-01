package com.devkor.ifive.nadab.domain.weeklyreport.application.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 주간 리포트 완성 이벤트
 * - 주간 리포트가 완성되면 발행
 * - 사용자에게 완성 알림 전송을 위한 트리거
 */
@Getter
@RequiredArgsConstructor
public class WeeklyReportCompletedEvent {

    private final Long reportId;
    private final Long userId;
}