package com.devkor.ifive.nadab.domain.weeklyreport.application.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 주간 리포트 제작 가능 이벤트
 * - 주간 리포트 제작 조건 충족 시 발행
 * - 사용자에게 제작 가능 알림 전송을 위한 트리거
 */
@Getter
@RequiredArgsConstructor
public class WeeklyReportAvailableEvent {

    private final Long userId;
}