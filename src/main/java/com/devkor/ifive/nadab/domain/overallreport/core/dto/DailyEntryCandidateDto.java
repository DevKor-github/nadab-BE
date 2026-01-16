package com.devkor.ifive.nadab.domain.overallreport.core.dto;

import java.time.LocalDate;

// 대표 샘플 후보 1건
public record DailyEntryCandidateDto(
        LocalDate date,
        String questionText,
        Integer questionLevel,
        String interestCode,
        String answerContent,
        String dailyReportContent,
        String emotionName
) {
    public int answerLength() {
        return answerContent == null ? 0 : answerContent.length();
    }
}
