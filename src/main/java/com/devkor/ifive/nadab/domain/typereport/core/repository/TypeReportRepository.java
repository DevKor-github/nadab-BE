package com.devkor.ifive.nadab.domain.typereport.core.repository;

import com.devkor.ifive.nadab.domain.typereport.core.entity.TypeReport;
import com.devkor.ifive.nadab.domain.typereport.core.entity.TypeReportStatus;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TypeReportRepository extends JpaRepository<TypeReport, Long> {

    Optional<TypeReport> findByUserIdAndInterestCodeAndStatusAndDeletedAtIsNull(Long userId, InterestCode interestCode, TypeReportStatus status);

    Optional<TypeReport> findTopByUserIdAndInterestCodeAndDeletedAtIsNullOrderByCreatedAtDesc(
            Long userId, InterestCode interestCode
    );


    // 활성 COMPLETED 리포트의 id 찾기
    @Query("""
        select tr.id
          from TypeReport tr
         where tr.user.id = :userId
           and tr.interestCode = :interestCode
           and tr.status = com.devkor.ifive.nadab.domain.typereport.core.entity.TypeReportStatus.COMPLETED
           and tr.deletedAt is null
    """)
    Optional<Long> findActiveCompletedId(@Param("userId") Long userId,
                                         @Param("interestCode") InterestCode interestCode);

    boolean existsByUserIdAndInterestCodeAndStatusAndDeletedAtIsNull(Long userId,
                                                                     InterestCode interestCode,
                                                                     TypeReportStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update TypeReport tr
           set tr.deletedAt = CURRENT_TIMESTAMP
         where tr.id = :id
           and tr.deletedAt is null
    """)
    int softDeleteById(@Param("id") Long id);

    // 통합 조회 (fetch join으로 analysisType까지 한번에)
    @Query("""
    select tr
      from TypeReport tr
      left join fetch tr.analysisType at
     where tr.user.id = :userId
       and tr.status = :status
       and tr.deletedAt is null
""")
    List<TypeReport> findAllActiveWithAnalysisType(
            @Param("userId") Long userId,
            @Param("status") TypeReportStatus status
    );

    @Query("""
    select tr
      from TypeReport tr
     where tr.user.id = :userId
       and tr.deletedAt is null
       and tr.createdAt = (
           select max(tr2.createdAt)
             from TypeReport tr2
            where tr2.user.id = :userId
              and tr2.interestCode = tr.interestCode
              and tr2.deletedAt is null
       )
""")
    List<TypeReport> findLatestAttemptsByUser(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE TypeReport t SET t.status = :status WHERE t.id = :id")
    int updateStatus(
            @Param("id") Long id,
            @Param("status") TypeReportStatus status
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    UPDATE TypeReport tr
       SET tr.status = :status,
           tr.analysisType = (
               SELECT at
                 FROM AnalysisType at
                WHERE at.code = :analysisTypeCode
                  AND at.deletedAt IS NULL
           ),
           tr.typeAnalysis = :typeAnalysis,
           tr.persona1Title = :persona1Title,
           tr.persona1Content = :persona1Content,
           tr.persona2Title = :persona2Title,
           tr.persona2Content = :persona2Content,
           tr.analyzedAt = CURRENT_TIMESTAMP
     WHERE tr.id = :reportId
       AND EXISTS (
           SELECT 1
             FROM AnalysisType at2
            WHERE at2.code = :analysisTypeCode
              AND at2.deletedAt IS NULL
       )
""")
    int markCompleted(
            @Param("reportId") Long reportId,
            @Param("status") TypeReportStatus status,
            @Param("analysisTypeCode") String analysisTypeCode,
            @Param("typeAnalysis") String typeAnalysis,
            @Param("persona1Title") String persona1Title,
            @Param("persona1Content") String persona1Content,
            @Param("persona2Title") String persona2Title,
            @Param("persona2Content") String persona2Content
    );


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    UPDATE TypeReport tr
       SET tr.status = :status,
           tr.analyzedAt = CURRENT_TIMESTAMP
     WHERE tr.id = :reportId
""")
    int markFailed(
            @Param("reportId") Long reportId,
            @Param("status") TypeReportStatus status
    );
}
