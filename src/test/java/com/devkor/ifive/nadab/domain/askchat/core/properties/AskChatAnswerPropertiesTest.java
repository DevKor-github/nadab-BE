package com.devkor.ifive.nadab.domain.askchat.core.properties;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class AskChatAnswerPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withUserConfiguration(PropertiesConfiguration.class);

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void application_configuration_binds_luna_with_low_reasoning_effort() {
        contextRunner.run(context -> {
            assertThat(context.getStartupFailure()).isNull();

            AskChatAnswerProperties properties = context.getBean(AskChatAnswerProperties.class);

            assertThat(properties.getModel()).isEqualTo("gpt-5.6-luna");
            assertThat(properties.getReasoningEffort()).isEqualTo("low");
        });
    }

    @Test
    void validation_rejects_blank_reasoning_effort() {
        AskChatAnswerProperties properties = new AskChatAnswerProperties();
        properties.setReasoningEffort(" ");

        assertThat(validator.validate(properties))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("reasoningEffort");
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(AskChatAnswerProperties.class)
    static class PropertiesConfiguration {
    }
}
