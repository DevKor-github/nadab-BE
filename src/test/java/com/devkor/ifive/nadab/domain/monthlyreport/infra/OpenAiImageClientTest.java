package com.devkor.ifive.nadab.domain.monthlyreport.infra;

import com.devkor.ifive.nadab.domain.monthlyreport.application.helper.MonthlyImagePromptComposer;
import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlyImagePromptContext;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyImageColorPalette;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyImageStylePreset;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OpenAiImageClientTest {

    @Test
    void context로_조립한_프롬프트를_OpenAI_요청에_사용한다() {
        ExchangeFunction exchangeFunction = mock(ExchangeFunction.class);
        when(exchangeFunction.exchange(any())).thenReturn(Mono.just(
                ClientResponse.create(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body("{\"data\":[{\"b64_json\":\"aW1hZ2U=\"}]}")
                        .build()
        ));
        WebClient.Builder webClientBuilder = WebClient.builder().exchangeFunction(exchangeFunction);
        MonthlyImagePromptComposer composer = mock(MonthlyImagePromptComposer.class);
        MonthlyImagePromptContext context = new MonthlyImagePromptContext(
                "요약", "코멘트", "키워드",
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31),
                MonthlyImageStylePreset.INK_WASH,
                MonthlyImageColorPalette.OCEAN_LIGHT
        );
        when(composer.compose(context)).thenReturn("composed-prompt");
        OpenAiImageClient client = new OpenAiImageClient(webClientBuilder, composer);
        ReflectionTestUtils.setField(client, "apiKey", "test-key");
        ReflectionTestUtils.setField(client, "model", "test-model");
        ReflectionTestUtils.setField(client, "size", "1024x1024");
        ReflectionTestUtils.setField(client, "quality", "medium");
        ReflectionTestUtils.setField(client, "outputFormat", "webp");

        String result = client.generateBase64Image(1L, context);

        assertThat(result).isEqualTo("aW1hZ2U=");
        verify(composer).compose(context);
    }
}
