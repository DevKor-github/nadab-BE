package com.devkor.ifive.nadab.domain.dailyreport.core.properties;

import com.devkor.ifive.nadab.domain.dailyreport.core.properties.DailyReportLlmProperties.ModelCandidate;
import com.devkor.ifive.nadab.domain.dailyreport.core.properties.DailyReportLlmProperties.TokenLimitParameter;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class DailyReportLlmPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withUserConfiguration(PropertiesConfiguration.class);

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void application_configuration_binds_two_equal_weight_candidates_with_model_specific_options() {
        contextRunner.run(context -> {
            assertThat(context.getStartupFailure()).isNull();

            DailyReportLlmProperties properties = context.getBean(DailyReportLlmProperties.class);

            assertThat(properties.getCandidates())
                    .extracting(ModelCandidate::getModel, ModelCandidate::getWeight)
                    .containsExactly(
                            tuple("gpt-4o-mini", 50),
                            tuple("gpt-5.6-luna", 50)
                    );

            ModelCandidate mini = properties.getCandidates().get(0);
            assertThat(mini.getTemperature()).isEqualTo(0.3);
            assertThat(mini.getMaxOutputTokens()).isEqualTo(512);
            assertThat(mini.getTokenLimitParameter()).isEqualTo(TokenLimitParameter.MAX_TOKENS);
            assertThat(mini.getReasoningEffort()).isNull();

            ModelCandidate luna = properties.getCandidates().get(1);
            assertThat(luna.getTemperature()).isEqualTo(1.0);
            assertThat(luna.getMaxOutputTokens()).isEqualTo(512);
            assertThat(luna.getTokenLimitParameter()).isEqualTo(TokenLimitParameter.MAX_COMPLETION_TOKENS);
            assertThat(luna.getReasoningEffort()).isEqualTo("none");
        });
    }

    @Test
    void dev_profile_inherits_shared_candidates_while_legacy_model_override_remains() {
        contextRunner
                .withPropertyValues("spring.profiles.active=dev")
                .run(context -> {
                    assertThat(context.getStartupFailure()).isNull();

                    DailyReportLlmProperties properties = context.getBean(DailyReportLlmProperties.class);

                    assertThat(properties.getModel()).isEqualTo("gpt-5.6-luna");
                    assertThat(properties.getCandidates())
                            .extracting(ModelCandidate::getModel, ModelCandidate::getWeight)
                            .containsExactly(
                                    tuple("gpt-4o-mini", 50),
                                    tuple("gpt-5.6-luna", 50)
                            );
                });
    }

    @Test
    void validation_rejects_candidate_weights_that_do_not_total_100() {
        DailyReportLlmProperties properties = propertiesWith(
                candidate("gpt-4o-mini", 40),
                candidate("gpt-5.6-luna", 50)
        );

        assertThat(validator.validate(properties))
                .extracting(ConstraintViolation::getMessage)
                .contains("daily-report.llm.candidates weights must total 100");
    }

    @Test
    void validation_rejects_duplicate_candidate_models() {
        DailyReportLlmProperties properties = propertiesWith(
                candidate("gpt-4o-mini", 50),
                candidate("gpt-4o-mini", 50)
        );

        assertThat(validator.validate(properties))
                .extracting(ConstraintViolation::getMessage)
                .contains("daily-report.llm.candidates models must be unique");
    }

    @Test
    void validation_rejects_invalid_nested_candidate_options() {
        ModelCandidate invalidCandidate = candidate("gpt-4o-mini", 100);
        invalidCandidate.setMaxOutputTokens(0);

        DailyReportLlmProperties properties = propertiesWith(invalidCandidate);

        assertThat(validator.validate(properties))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("candidates[0].maxOutputTokens");
    }

    private DailyReportLlmProperties propertiesWith(ModelCandidate... candidates) {
        DailyReportLlmProperties properties = new DailyReportLlmProperties();
        properties.setCandidates(List.of(candidates));
        return properties;
    }

    private ModelCandidate candidate(String model, int weight) {
        ModelCandidate candidate = new ModelCandidate();
        candidate.setModel(model);
        candidate.setWeight(weight);
        candidate.setTemperature(0.3);
        candidate.setMaxOutputTokens(512);
        candidate.setTokenLimitParameter(TokenLimitParameter.MAX_TOKENS);
        return candidate;
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(DailyReportLlmProperties.class)
    static class PropertiesConfiguration {
    }
}
