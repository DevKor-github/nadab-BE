package com.devkor.ifive.nadab.domain.monthlyreport.core.repository;

import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface MonthlyReportV2Repository extends JpaRepository<MonthlyReportV2, Long> {

    boolean existsByUserId(Long userId);

    Optional<MonthlyReportV2> findByUserIdAndMonthStartDate(Long userId, LocalDate monthStartDate);

    Optional<MonthlyReportV2> findByUserIdAndMonthStartDateAndStatus(
            Long userId,
            LocalDate monthStartDate,
            MonthlyReportStatus status
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE MonthlyReportV2 m SET m.status = :status WHERE m.id = :id")
    int updateStatus(
            @Param("id") Long id,
            @Param("status") MonthlyReportStatus status
    );
}
