package com.devkor.ifive.nadab.global.core.config.infra;

import com.modernmt.text.profanity.ProfanityFilter;
import com.vane.badwordfiltering.BadWordFiltering;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ProfanityFilterConfig {

    @Bean
    public ProfanityFilter profanityFilter() {
        return new ProfanityFilter();
    }

    @Bean
    public BadWordFiltering badWordFiltering() {
        return new BadWordFiltering();
    }
}
