package com.devkor.ifive.nadab.global.exception;

import com.devkor.ifive.nadab.domain.pdfexport.api.dto.response.PdfExportInProgressResponse;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import lombok.Getter;

/**
 * 생성 중인 작업이 있는데 다른 조건으로 또 요청했을 때(유저당 동시 1개 제한 위반).
 * 이미 생성 중인 작업의 id를 실어, 클라가 그 생성 화면으로 이동할 수 있게 한다.
 */
@Getter
public class PdfExportInProgressException extends ConflictException {
    private final PdfExportInProgressResponse inProgressJob;

    public PdfExportInProgressException(Long jobId) {
        super(ErrorCode.PDF_EXPORT_ALREADY_IN_PROGRESS);
        this.inProgressJob = new PdfExportInProgressResponse(jobId);
    }
}