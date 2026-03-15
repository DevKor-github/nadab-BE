package com.devkor.ifive.nadab.domain.terms.core.repository;

import com.devkor.ifive.nadab.domain.terms.core.entity.Term;
import com.devkor.ifive.nadab.domain.terms.core.entity.TermsType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TermRepository extends JpaRepository<Term, Long> {

    Optional<Term> findByTermsTypeAndIsActiveTrue(TermsType termsType);

    List<Term> findByIsActiveTrue();
}