package com.devkor.ifive.nadab.domain.monthlyreport.infra;

import com.devkor.ifive.nadab.domain.monthlyreport.application.helper.MonthlyImagePromptComposer;
import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlyImagePromptContext;
import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.OpenAiImageGenerateRequestDto;
import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.OpenAiImageGenerateResponseDto;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ai.AiResponseParseException;
import com.devkor.ifive.nadab.global.exception.ai.AiServiceUnavailableException;
import com.devkor.ifive.nadab.global.infra.llm.LlmExceptionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAiImageClient {

    private final WebClient.Builder webClientBuilder;
    private final MonthlyImagePromptComposer monthlyImagePromptComposer;

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.image.model}")
    private String model;

    @Value("${openai.image.size}")
    private String size;

    @Value("${openai.image.quality}")
    private String quality;

    @Value("${openai.image.output-format}")
    private String outputFormat;

    public String generateBase64Image(Long userId, MonthlyImagePromptContext context) {
        return generateBase64Image(userId, monthlyImagePromptComposer.compose(context));
    }

    private String generateBase64Image(Long userId, String prompt) {

        OpenAiImageGenerateRequestDto request = new OpenAiImageGenerateRequestDto(
                model,
                prompt,
                size,
                quality,
                outputFormat,
                1,
                String.valueOf(userId)
        );

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer ->
                        configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)
                )
                .build();

        OpenAiImageGenerateResponseDto response;
        try {
            response = webClientBuilder
                    .baseUrl("https://api.openai.com")
                    .exchangeStrategies(strategies)
                    .build()
                    .post()
                    .uri("/v1/images/generations")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OpenAiImageGenerateResponseDto.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("[OPENAI_IMAGE][HTTP_ERROR] userId={}, status={}, responseBody={}",
                    userId, e.getStatusCode().value(), truncate(e.getResponseBodyAsString()), e);
            throw LlmExceptionMapper.toUnavailable(ErrorCode.AI_NO_RESPONSE, e);
        } catch (Exception e) {
            log.error("[OPENAI_IMAGE][CALL_FAILED] userId={}, message={}", userId, e.getMessage(), e);
            throw LlmExceptionMapper.toUnavailable(ErrorCode.AI_NO_RESPONSE, e);
        }

        if (response == null) {
            log.error("[OPENAI_IMAGE][EMPTY_RESPONSE] userId={}", userId);
            throw new AiServiceUnavailableException(ErrorCode.AI_NO_RESPONSE);
        }

        try {
            return response.firstBase64Image();
        } catch (AiResponseParseException e) {
            log.error("[OPENAI_IMAGE][PARSE_FAILED] userId={}, reason=invalid_data_shape", userId, e);
            throw e;
        } catch (Exception e) {
            log.error("[OPENAI_IMAGE][PARSE_FAILED] userId={}, reason=unexpected_exception, message={}",
                    userId, e.getMessage(), e);
            throw new AiResponseParseException(ErrorCode.AI_RESPONSE_PARSE_FAILED);
        }
    }

    private String truncate(String value) {
        if (value == null) {
            return "";
        }
        int max = 1200;
        return value.length() <= max ? value : value.substring(0, max) + "...(truncated)";
    }
}
