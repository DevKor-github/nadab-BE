package com.devkor.ifive.nadab.domain.pdfexport.application.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * PDF 내보내기 실패 FCM 알림 트리거.
 * - failAndRefund 로 모이는 두 경로(렌더 리스너·복구 스케줄러)가 그 반환값이 true(=이 호출이 실제로 환불)일 때만 트랜잭션 커밋 후 발행한다.
 * - 경합에서 진 호출은 이미 완료·환불된 job 이라 발행하지 않는다.
 */
@Getter
@RequiredArgsConstructor
public class PdfExportFailedEvent {

    private final Long jobId;
    private final Long userId;
}