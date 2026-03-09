package com.devkor.ifive.nadab.domain.typereport.core.repository;

import com.devkor.ifive.nadab.domain.typereport.core.dto.AnalysisTypeCandidateDto;
import com.devkor.ifive.nadab.domain.typereport.core.entity.AnalysisType;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnalysisTypeRepository extends JpaRepository<AnalysisType, Long> {

    @Query("""
        select new com.devkor.ifive.nadab.domain.typereport.core.dto.AnalysisTypeCandidateDto(
            a.code, a.name, a.description, a.hashtag1, a.hashtag2, a.hashtag3
        )
        from AnalysisType a
        where a.interestCode = :interestCode
          and a.deletedAt is null
        order by a.code asc
    """)
    List<AnalysisTypeCandidateDto> findCandidatesByInterestCode(@Param("interestCode") InterestCode interestCode);

}
