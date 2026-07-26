package com.devkor.ifive.nadab.domain.pdfexport.application.helper;

import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportJob;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 다운로드 파일명 검증. 업로드 시 S3 에 각인하는 이름과 응답 DTO 의 이름이 같은 유틸에서 나온다.
 */
class PdfExportFileNamesTest {

    @Test
    void 한글_파일명은_기간을_yyyyMMdd_로_담는다() {
        PdfExportJob job = job("2025-11-01", "2025-11-30");

        assertThat(PdfExportFileNames.downloadFileName(job))
                .isEqualTo("나답_나에게답하다_20251101-20251130.pdf");
    }

    @Test
    void ascii_폴백은_비ascii_문자를_담지_않는다() {
        PdfExportJob job = job("2025-11-01", "2025-11-30");

        String fallback = PdfExportFileNames.asciiFallbackFileName(job);

        assertThat(fallback).isEqualTo("nadab_20251101-20251130.pdf");
        // Content-Disposition 의 filename= 에 그대로 들어가므로 여기 비ASCII 가 섞이면 헤더가 깨진다.
        assertThat(fallback).matches("\\p{ASCII}+");
    }

    @Test
    void 시작일과_종료일이_같아도_두_날짜를_모두_적는다() {
        PdfExportJob job = job("2026-01-01", "2026-01-01");

        assertThat(PdfExportFileNames.downloadFileName(job))
                .isEqualTo("나답_나에게답하다_20260101-20260101.pdf");
    }

    /** 파일명은 기간만 쓰므로 user·resultKey 는 판정에 영향이 없다. */
    private PdfExportJob job(String start, String end) {
        return PdfExportJob.createPending(null, PdfExportType.REPORT_AND_ANSWER,
                LocalDate.parse(start), LocalDate.parse(end), "key");
    }
}