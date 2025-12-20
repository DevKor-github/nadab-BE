package com.devkor.ifive.nadab.domain.terms.application.dto;

import com.devkor.ifive.nadab.domain.terms.core.entity.TermsType;

import java.util.List;

public record TermsConsentInfo(
        Boolean requiresConsent,
        List<TermsType> missingTerms,
        Boolean service,
        Boolean privacy,
        Boolean ageVerification,
        Boolean marketing
) {
}