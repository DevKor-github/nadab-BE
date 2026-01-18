package com.devkor.ifive.nadab.domain.overallreport.core.dto;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.EmotionName;

import java.time.LocalDate;

public record DailyEntryWithIdDto(
        String id,
        LocalDate date,
        String question,
        String answer,
        String dailyReport,
        EmotionName emotion
) {}
