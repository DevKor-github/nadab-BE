package com.devkor.ifive.nadab.domain.dailyreport.core.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

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

    @Valid
    @NotEmpty
    private List<@NotNull ModelCandidate> candidates = new ArrayList<>();

    @AssertTrue(message = "daily-report.llm.candidates weights must total 100")
    public boolean isCandidateWeightTotalValid() {
        if (candidates == null || candidates.isEmpty()) {
            return true;
        }

        return candidates.stream()
                .filter(Objects::nonNull)
                .mapToInt(ModelCandidate::getWeight)
                .sum() == 100;
    }

    @AssertTrue(message = "daily-report.llm.candidates models must be unique")
    public boolean isCandidateModelUnique() {
        if (candidates == null || candidates.isEmpty()) {
            return true;
        }

        List<String> models = candidates.stream()
                .filter(Objects::nonNull)
                .map(ModelCandidate::getModel)
                .filter(Objects::nonNull)
                .toList();

        return new HashSet<>(models).size() == models.size();
    }

    @Getter
    @Setter
    public static class ModelCandidate {

        @NotBlank
        private String model;

        @Min(1)
        @Max(100)
        private int weight;

        @DecimalMin("0.0")
        @DecimalMax("2.0")
        private double temperature;

        @Min(1)
        private int maxOutputTokens;

        @NotNull
        private TokenLimitParameter tokenLimitParameter;

        private String reasoningEffort;
    }

    public enum TokenLimitParameter {
        MAX_TOKENS,
        MAX_COMPLETION_TOKENS
    }
}
