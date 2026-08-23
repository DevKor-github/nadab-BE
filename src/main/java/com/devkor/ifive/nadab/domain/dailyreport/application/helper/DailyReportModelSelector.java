package com.devkor.ifive.nadab.domain.dailyreport.application.helper;

import com.devkor.ifive.nadab.domain.dailyreport.core.properties.DailyReportLlmProperties;
import com.devkor.ifive.nadab.domain.dailyreport.core.properties.DailyReportLlmProperties.ModelCandidate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntUnaryOperator;

@Component
public class DailyReportModelSelector {

    private static final int TOTAL_WEIGHT = 100;

    private final DailyReportLlmProperties properties;
    private final IntUnaryOperator randomValueGenerator;

    @Autowired
    public DailyReportModelSelector(DailyReportLlmProperties properties) {
        this(properties, bound -> ThreadLocalRandom.current().nextInt(bound));
    }

    DailyReportModelSelector(
            DailyReportLlmProperties properties,
            IntUnaryOperator randomValueGenerator
    ) {
        this.properties = properties;
        this.randomValueGenerator = randomValueGenerator;
    }

    public ModelCandidate select() {
        int randomValue = randomValueGenerator.applyAsInt(TOTAL_WEIGHT);
        int cumulativeWeight = 0;

        for (ModelCandidate candidate : properties.getCandidates()) {
            cumulativeWeight += candidate.getWeight();
            if (randomValue < cumulativeWeight) {
                return candidate;
            }
        }

        throw new IllegalStateException("Failed to select a DailyReport LLM model candidate");
    }
}
