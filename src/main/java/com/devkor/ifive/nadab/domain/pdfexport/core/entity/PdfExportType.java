package com.devkor.ifive.nadab.domain.pdfexport.core.entity;

/**
 * PDF 내보내기 유형과 크리스탈 비용.
 * - REPORT_ONLY: 리포트(주간/월간)만 (50)
 * - ANSWER_ONLY: 답변만 (50)
 * - REPORT_AND_ANSWER: 리포트 + 답변 (100)
 */
public enum PdfExportType {
    REPORT_ONLY(50L),
    ANSWER_ONLY(50L),
    REPORT_AND_ANSWER(100L);

    private final long crystalCost;

    PdfExportType(long crystalCost) {
        this.crystalCost = crystalCost;
    }

    public long getCrystalCost() {
        return crystalCost;
    }

    public boolean includesAnswer() {
        return this == ANSWER_ONLY || this == REPORT_AND_ANSWER;
    }

    public boolean includesReport() {
        return this == REPORT_ONLY || this == REPORT_AND_ANSWER;
    }
}