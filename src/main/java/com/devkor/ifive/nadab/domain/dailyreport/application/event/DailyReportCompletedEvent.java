package com.devkor.ifive.nadab.domain.dailyreport.application.event;

import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 일일 리포트 완성 이벤트
 * - 답변이 완성되면 발행
 * - 유형 리포트 제작 가능 알림 체크를 위한 트리거
 */
@Getter
@RequiredArgsConstructor
public class DailyReportCompletedEvent {

    private final Long userId;
    private final InterestCode interestCode;
}