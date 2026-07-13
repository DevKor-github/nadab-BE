package com.devkor.ifive.nadab.domain.askchat.core.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "ask-chat.rag")
public class AskChatRagProperties {

    @NotBlank
    private String embeddingModel = "text-embedding-3-small";

    @Min(1)
    private int embeddingDimensions = 1536;

    @Min(1)
    private int embeddingVersion = 1;

    @Min(1)
    private int embeddingBatchSize = 20;

    @Min(1)
    private int retrievalLimit = 5;
}
