package com.devkor.ifive.nadab.domain.dailyreport.core.properties;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "daily-report.llm")
public class DailyReportLlmProperties {

    @NotBlank
    private String model = "gpt-4o-mini";

    @DecimalMin("0.0")
    @DecimalMax("2.0")
    private double temperature = 0.3;

    @Min(1)
    private int maxOutputTokens = 512;

    @NotNull
    private TokenLimitParameter tokenLimitParameter = TokenLimitParameter.MAX_TOKENS;

    private String reasoningEffort;

    public enum TokenLimitParameter {
        MAX_TOKENS,
        MAX_COMPLETION_TOKENS
    }
}
