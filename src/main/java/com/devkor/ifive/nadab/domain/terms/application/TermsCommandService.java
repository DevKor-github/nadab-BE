package com.devkor.ifive.nadab.domain.terms.application;

import com.devkor.ifive.nadab.domain.terms.core.entity.Term;
import com.devkor.ifive.nadab.domain.terms.core.entity.TermsType;
import com.devkor.ifive.nadab.domain.terms.core.entity.UserTerm;
import com.devkor.ifive.nadab.domain.terms.core.repository.TermRepository;
import com.devkor.ifive.nadab.domain.terms.core.repository.UserTermRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TermsCommandService {

    private final TermRepository termRepository;
    private final UserTermRepository userTermRepository;
    private final UserRepository userRepository;

    public void saveConsents(Long userId, Boolean service, Boolean privacy, Boolean ageVerification, Boolean marketing) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다"));

        // 필수 약관 검증
        validateRequiredTerms(service, privacy, ageVerification);

        // 한 번에 조회
        List<Term> activeTerms = termRepository.findByIsActiveTrue();
        List<UserTerm> existingUserTerms = userTermRepository.findByUserId(userId);

        // 각 약관별로 UserTerm 생성 또는 업데이트
        processConsent(user, activeTerms, existingUserTerms, TermsType.SERVICE, service);
        processConsent(user, activeTerms, existingUserTerms, TermsType.PRIVACY, privacy);
        processConsent(user, activeTerms, existingUserTerms, TermsType.AGE_VERIFICATION, ageVerification);
        processConsent(user, activeTerms, existingUserTerms, TermsType.MARKETING, marketing);


        log.info("약관 동의 처리 완료: userId={}", userId);
    }

    public void updateMarketingConsent(Long userId, Boolean agreed) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다"));

        processConsent(user, TermsType.MARKETING, agreed);
        log.info("마케팅 동의 변경: userId={}, agreed={}", userId, agreed);
    }

    private void processConsent(User user, TermsType termsType, Boolean agreed) {
        // 최신 활성화된 약관 조회
        Term term = termRepository.findByTermsTypeAndIsActiveTrue(termsType)
                .orElseThrow(() -> new NotFoundException("약관을 찾을 수 없습니다. termsType: " + termsType));

        Optional<UserTerm> existingUserTerm = userTermRepository.findByUserIdAndTermId(user.getId(), term.getId());

        if (existingUserTerm.isPresent()) {
            // 기존 레코드 업데이트
            UserTerm userTerm = existingUserTerm.get();
            if (agreed) {
                userTerm.reAgree();
            } else {
                userTerm.withdraw();
            }
        } else {
            // 새 레코드 생성 및 저장
            UserTerm newUserTerm = UserTerm.create(user, term, agreed);
            userTermRepository.save(newUserTerm);
        }
    }

    private void processConsent(User user, List<Term> activeTerms, List<UserTerm> existingUserTerms, TermsType termsType, Boolean agreed) {
        // activeTerms에서 해당 타입 찾기
        Term term = activeTerms.stream()
                .filter(t -> t.getTermsType() == termsType)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("약관을 찾을 수 없습니다. termsType: " + termsType));

        // existingUserTerms에서 해당 약관 찾기
        UserTerm existingUserTerm = existingUserTerms.stream()
                .filter(ut -> ut.getTerm().getId().equals(term.getId()))
                .findFirst()
                .orElse(null);

        if (existingUserTerm != null) {
            // 기존 레코드 업데이트
            if (agreed) {
                existingUserTerm.reAgree();
            } else {
                existingUserTerm.withdraw();
            }
        } else {
            // 새 레코드 생성 및 저장
            UserTerm newUserTerm = UserTerm.create(user, term, agreed);
            userTermRepository.save(newUserTerm);
        }
    }

    private void validateRequiredTerms(Boolean service, Boolean privacy, Boolean ageVerification) {
        if (!service) {
            throw new BadRequestException("서비스 이용약관에 동의해야 합니다");
        }
        if (!privacy) {
            throw new BadRequestException("개인정보 처리방침에 동의해야 합니다");
        }
        if (!ageVerification) {
            throw new BadRequestException("만 14세 이상 확인에 동의해야 합니다");
        }
    }
}