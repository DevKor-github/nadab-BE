package com.devkor.ifive.nadab.global.core.prompt.monthly;

public interface MonthlyReportPromptLoader {
    String loadV1Prompt();

    String loadV2BaselinePrompt();

    String loadV2ComparisonPrompt();
}
