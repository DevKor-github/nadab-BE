package com.devkor.ifive.nadab.domain.pdfexport.application.helper;

import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportJob;

import java.time.format.DateTimeFormatter;

/**
 * 다운로드 파일명 공용 유틸. 두 곳에서 같은 파일명이 필요하다:
 * - 업로드(리스너): S3 객체에 Content-Disposition으로 각인(CloudFront는 발급 시 동적 오버라이드 불가)
 * - 발급(QueryService): 응답 DTO의 평문 fileName(앱 네이티브 저장용)
 * 파일명은 job 기간에서 결정적이라 업로드 시점에 이미 안다.
 */
public final class PdfExportFileNames {

    private static final DateTimeFormatter FILENAME_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String DOWNLOAD_PREFIX = "나답_";
    private static final String DOWNLOAD_ASCII_PREFIX = "nadab_";

    private PdfExportFileNames() {
    }

    /** 저장 파일명(한글): 나답_20251101-20251130.pdf */
    public static String downloadFileName(PdfExportJob job) {
        return build(DOWNLOAD_PREFIX, job);
    }

    /** Content-Disposition ASCII 폴백 파일명: nadab_20251101-20251130.pdf */
    public static String asciiFallbackFileName(PdfExportJob job) {
        return build(DOWNLOAD_ASCII_PREFIX, job);
    }

    private static String build(String prefix, PdfExportJob job) {
        return "%s%s-%s.pdf".formatted(
                prefix,
                job.getStartDate().format(FILENAME_DATE),
                job.getEndDate().format(FILENAME_DATE));
    }
}