package com.devkor.ifive.nadab.domain.typereport.core.repository;

import com.devkor.ifive.nadab.domain.typereport.core.entity.TypeReport;
import com.devkor.ifive.nadab.domain.typereport.core.entity.TypeReportStatus;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TypeReportRepository extends JpaRepository<TypeReport, Long> {

    Optional<TypeReport> findByUserIdAndInterestCode(Long userId, InterestCode interestCode);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE TypeReport t SET t.status = :status WHERE t.id = :id")
    int updateStatus(
            @Param("id") Long id,
            @Param("status") TypeReportStatus status
    );
}
