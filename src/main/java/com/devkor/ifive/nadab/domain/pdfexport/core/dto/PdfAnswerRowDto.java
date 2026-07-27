package com.devkor.ifive.nadab.domain.pdfexport.core.dto;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.EmotionCode;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;

import java.time.LocalDate;

/**
 * PDF 기간 조회 - 답변 1건(질문·관심사·감정 조인 결과).
 * - imageKey: 사진 없으면 null
 * - interestCode: 질문에 관심사가 없으면 null (관심사 태그용)
 * - emotionCode: 답변의 완료 일간 리포트가 없거나 감정 미분석이면 null (감정 태그용)
 */
public record PdfAnswerRowDto(
        LocalDate date,
        String content,
        String imageKey,
        String questionText,
        InterestCode interestCode,
        EmotionCode emotionCode
) {
}