package com.devkor.ifive.nadab.domain.stats.application;

import com.devkor.ifive.nadab.domain.stats.core.repository.PeakStatsRepository;
import com.devkor.ifive.nadab.domain.stats.core.repository.PeakStatsStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PeakStatsInitializer implements ApplicationRunner {

    private final PeakStatsRepository peakStatsRepository;
    private final PeakStatsStore peakStatsStore;

    @Override
    public void run(ApplicationArguments args) {
        try {
            var peaks = peakStatsRepository.findAllPeakStats();
            peaks.forEach(peakStatsStore::updateIfGreater);
            log.info("Peak statistics initialized: {} metrics", peaks.size());
        } catch (RuntimeException e) {
            log.error("Failed to initialize peak statistics", e);
        }
    }
}
