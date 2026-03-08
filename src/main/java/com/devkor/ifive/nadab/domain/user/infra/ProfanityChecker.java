package com.devkor.ifive.nadab.domain.user.infra;

import com.modernmt.text.profanity.ProfanityFilter;
import com.vane.badwordfiltering.BadWordFiltering;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProfanityChecker {

    private final BadWordFiltering koFilter;
    private final ProfanityFilter enFilter;

    @PostConstruct
    void init() {
        koFilter.add("좆");
    }

    public boolean containsProfanity(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        if (enFilter.test("en", text)) {
            return true;
        }

        if (koFilter.check(text)) {
            return true;
        }

        return false;
    }
}
