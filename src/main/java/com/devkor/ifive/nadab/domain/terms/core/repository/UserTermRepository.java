package com.devkor.ifive.nadab.domain.terms.core.repository;

import com.devkor.ifive.nadab.domain.terms.core.entity.UserTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserTermRepository extends JpaRepository<UserTerm, Long> {

    // TermsQueryService에서 ut.getTerm().getId() 호출 시 N+1 쿼리 방지를 위해 JOIN FETCH 사용
    @Query("SELECT ut FROM UserTerm ut JOIN FETCH ut.term WHERE ut.user.id = :userId")
    List<UserTerm> findByUserId(@Param("userId") Long userId);

    Optional<UserTerm> findByUserIdAndTermId(Long userId, Long termId);
}