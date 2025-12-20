package com.devkor.ifive.nadab.domain.terms.application;

import com.devkor.ifive.nadab.domain.terms.application.dto.TermsConsentInfoDto;
import com.devkor.ifive.nadab.domain.terms.core.entity.Term;
import com.devkor.ifive.nadab.domain.terms.core.entity.TermsType;
import com.devkor.ifive.nadab.domain.terms.core.entity.UserTerm;
import com.devkor.ifive.nadab.domain.terms.core.repository.TermRepository;
import com.devkor.ifive.nadab.domain.terms.core.repository.UserTermRepository;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TermsQueryService {

    private final TermRepository termRepository;
    private final UserTermRepository userTermRepository;

    // 약관 동의 정보 조회 (재동의 필요 약관 + 현재 동의 상태)
    public TermsConsentInfoDto getTermsConsentInfo(Long userId) {
        List<Term> activeTerms = termRepository.findByIsActiveTrue();
        List<UserTerm> userTerms = userTermRepository.findByUserId(userId);

        // userTerms를 Map으로 변환
        Map<Long, Boolean> userTermMap = userTerms.stream()
                .collect(Collectors.toMap(
                        ut -> ut.getTerm().getId(),
                        UserTerm::getAgreed
                ));

        // 초기화
        Boolean service = false;
        Boolean privacy = false;
        Boolean ageVerification = false;
        Boolean marketing = false;
        List<TermsType> missingTerms = new ArrayList<>();

        // 한 번만 순회하며 모든 정보 수집
        for (Term term : activeTerms) {
            boolean agreed = userTermMap.getOrDefault(term.getId(), false);

            switch (term.getTermsType()) {
                case SERVICE -> service = agreed;
                case PRIVACY -> privacy = agreed;
                case AGE_VERIFICATION -> ageVerification = agreed;
                case MARKETING -> marketing = agreed;
            }

            if (!agreed) {
                missingTerms.add(term.getTermsType());
            }
        }

        Boolean requiresConsent = !missingTerms.isEmpty();
        return new TermsConsentInfoDto(requiresConsent, missingTerms, service, privacy, ageVerification, marketing);

    }

    public boolean hasAgreedToMarketing(Long userId) {
        Term marketingTerm = termRepository.findByTermsTypeAndIsActiveTrue(TermsType.MARKETING)
                .orElseThrow(() -> new NotFoundException("마케팅 약관을 찾을 수 없습니다"));

        return userTermRepository.findByUserIdAndTermId(userId, marketingTerm.getId())
                .map(UserTerm::getAgreed)
                .orElse(false);
    }
}