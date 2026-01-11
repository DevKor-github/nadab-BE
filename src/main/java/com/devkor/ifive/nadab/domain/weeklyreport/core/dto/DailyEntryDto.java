package com.devkor.ifive.nadab.domain.weeklyreport.core.dto;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.EmotionName;

import java.time.LocalDate;

public record DailyEntryDto(
        LocalDate date,
        String question,
        String answer,
        String dailyReport,
        EmotionName emotion
) {
}
