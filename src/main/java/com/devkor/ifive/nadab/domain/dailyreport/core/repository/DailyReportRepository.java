package com.devkor.ifive.nadab.domain.dailyreport.core.repository;

import com.devkor.ifive.nadab.domain.dailyreport.core.dto.AnswerDetailDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.FeedDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.InterestCompletedCountDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReport;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReportStatus;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyReportRepository extends JpaRepository<DailyReport, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE DailyReport r
        SET r.status = com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReportStatus.COMPLETED,
            r.content = :content,
            r.emotion.id = :emotionId,
            r.analyzedAt = CURRENT_TIMESTAMP
        WHERE r.id = :reportId
    """)
    int markCompleted(@Param("reportId") Long reportId, @Param("content") String content, @Param("emotionId") Long emotionId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE DailyReport r
        SET r.status = com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReportStatus.FAILED
        WHERE r.id = :reportId
    """)
    int markFailed(@Param("reportId") Long reportId);

    Optional<DailyReport> findByAnswerEntryAndDate(AnswerEntry answerEntry, LocalDate date);

    /**
     * 리포트 ID(DailyReport.id)로 답변 상세 조회
     */
    @Query("""
        select new com.devkor.ifive.nadab.domain.dailyreport.core.dto.AnswerDetailDto(
            q.questionText,
            i.code,
            ae.date,
            ae.content,
            dr.content,
            e.code
        )
        from DailyReport dr
        join dr.answerEntry ae
        join ae.question q
        left join q.interest i
        left join dr.emotion e
        where dr.id = :reportId
        and dr.status = com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReportStatus.COMPLETED
        and ae.user.id = :userId
""")
    Optional<AnswerDetailDto> findDetailByReportId(
            @Param("userId") Long userId,
            @Param("reportId") Long reportId
    );

    /**
     * 특정 유저의 주간 범위 내 COMPLETED 일간 리포트 개수를 반환합니다.
     * (DailyReport -> AnswerEntry -> User 조인)
     */
    long countByAnswerEntry_User_IdAndStatusAndDateBetween(
            Long userId,
            DailyReportStatus status,
            LocalDate weekStartDate,
            LocalDate weekEndDate
    );

    /**
     * 편의 메서드: COMPLETED만 카운트
     */
    default long countCompletedInWeek(Long userId, LocalDate weekStartDate, LocalDate weekEndDate) {
        return countByAnswerEntry_User_IdAndStatusAndDateBetween(
                userId,
                DailyReportStatus.COMPLETED,
                weekStartDate,
                weekEndDate
        );
    }

    default long countCompletedInMonth(Long userId, LocalDate monthStartDate, LocalDate monthEndDate) {
        return countByAnswerEntry_User_IdAndStatusAndDateBetween(
                userId,
                DailyReportStatus.COMPLETED,
                monthStartDate,
                monthEndDate
        );
    }

    @Query("""
        select count(dr)
          from DailyReport dr
          join dr.answerEntry ae
          join ae.question q
          join q.interest i
         where ae.user.id = :userId
           and i.code = :interestCode
           and dr.status = :status
    """)
    long countByUserIdAndInterestCodeAndStatus(
            @Param("userId") Long userId,
            @Param("interestCode") InterestCode interestCode,
            @Param("status") DailyReportStatus status
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE DailyReport w SET w.status = :status WHERE w.id = :id")
    int updateStatus(
            @Param("id") Long id,
            @Param("status") DailyReportStatus status
    );

    @Query("""
        select new com.devkor.ifive.nadab.domain.dailyreport.core.dto.FeedDto(
            ae.user.nickname,
            ae.user.profileImageKey,
            ae.user.defaultProfileType,
            ae.question.interest.code,
            ae.question.questionText,
            ae.content,
            dr.emotion.code
        )
        from DailyReport dr
        join AnswerEntry ae on dr.answerEntry = ae
        where dr.date = :date
          and dr.isShared = true
          and dr.status = com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReportStatus.COMPLETED
          and ae.user.id in :friendIds
          and ae.user.deletedAt is null
        order by dr.createdAt desc
    """)
    List<FeedDto> findSharedFeedsByFriendIds(
        @Param("date") LocalDate date,
        @Param("friendIds") List<Long> friendIds
    );

    @Query("""
        select dr
        from DailyReport dr
        join fetch dr.answerEntry ae
        where ae.user.id = :userId
          and dr.date = :date
          and dr.status = com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReportStatus.COMPLETED
    """)
    Optional<DailyReport> findByUserIdAndDate(
        @Param("userId") Long userId,
        @Param("date") LocalDate date
    );

    @Query("""
        select new com.devkor.ifive.nadab.domain.dailyreport.core.dto.InterestCompletedCountDto(
            i.code,
            count(dr)
        )
          from DailyReport dr
          join dr.answerEntry ae
          join ae.question q
          join q.interest i
         where ae.user.id = :userId
           and dr.status = :status
         group by i.code
    """)
    List<InterestCompletedCountDto> countCompletedByInterest(
            @Param("userId") Long userId,
            @Param("status") DailyReportStatus status
    );
}
