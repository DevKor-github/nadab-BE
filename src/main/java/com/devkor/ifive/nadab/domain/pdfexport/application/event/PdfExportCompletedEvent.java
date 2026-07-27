package com.devkor.ifive.nadab.domain.pdfexport.application.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * PDF 내보내기 완성 이벤트.
 * - 렌더·업로드·confirm(COMPLETED 전이)이 끝난 직후 발행.
 * - 완료 FCM 알림 발송 트리거.
 */
@Getter
@RequiredArgsConstructor
public class PdfExportCompletedEvent {

    private final Long jobId;
    private final Long userId;
}