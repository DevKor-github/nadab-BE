package com.devkor.ifive.nadab.domain.dailyreport.application.helper;

import com.devkor.ifive.nadab.domain.dailyreport.core.properties.DailyReportLlmProperties;
import com.devkor.ifive.nadab.domain.dailyreport.core.properties.DailyReportLlmProperties.ModelCandidate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class DailyReportModelSelectorTest {

    @ParameterizedTest
    @CsvSource({
            "0, gpt-4o-mini",
            "49, gpt-4o-mini",
            "50, gpt-5.6-luna",
            "99, gpt-5.6-luna"
    })
    void select_maps_random_value_to_weighted_candidate(int randomValue, String expectedModel) {
        DailyReportLlmProperties properties = new DailyReportLlmProperties();
        properties.setCandidates(List.of(
                candidate("gpt-4o-mini", 50),
                candidate("gpt-5.6-luna", 50)
        ));
        DailyReportModelSelector selector = new DailyReportModelSelector(properties, ignored -> randomValue);

        ModelCandidate selected = selector.select();

        assertThat(selected.getModel()).isEqualTo(expectedModel);
    }

    @Test
    void select_draws_again_on_each_invocation() {
        DailyReportLlmProperties properties = new DailyReportLlmProperties();
        properties.setCandidates(List.of(
                candidate("gpt-4o-mini", 50),
                candidate("gpt-5.6-luna", 50)
        ));
        int[] randomValues = {0, 50};
        AtomicInteger invocationCount = new AtomicInteger();
        DailyReportModelSelector selector = new DailyReportModelSelector(
                properties,
                ignored -> randomValues[invocationCount.getAndIncrement()]
        );

        ModelCandidate first = selector.select();
        ModelCandidate second = selector.select();

        assertThat(first.getModel()).isEqualTo("gpt-4o-mini");
        assertThat(second.getModel()).isEqualTo("gpt-5.6-luna");
        assertThat(invocationCount).hasValue(2);
    }

    private ModelCandidate candidate(String model, int weight) {
        ModelCandidate candidate = new ModelCandidate();
        candidate.setModel(model);
        candidate.setWeight(weight);
        return candidate;
    }
}
