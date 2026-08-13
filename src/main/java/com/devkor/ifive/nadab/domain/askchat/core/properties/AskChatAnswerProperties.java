package com.devkor.ifive.nadab.domain.askchat.core.properties;

import com.devkor.ifive.nadab.global.infra.llm.LlmProvider;
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
@ConfigurationProperties(prefix = "ask-chat.answer")
public class AskChatAnswerProperties {

    @NotNull
    private LlmProvider provider = LlmProvider.OPENAI;

    @NotBlank
    private String model = "gpt-5.6-luna";

    @DecimalMin("0.0")
    @DecimalMax("2.0")
    private double temperature = 1.0;

    @Min(1)
    private int maxTokens = 900;

    @Min(1)
    private int recentMessageLimit = 10;

    @Min(0)
    private int followUpQuestionCount = 2;

    @Min(1)
    private int promptVersion = 2;
}
