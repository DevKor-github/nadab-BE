package com.devkor.ifive.nadab.domain.like.core.repository;

import com.devkor.ifive.nadab.domain.like.core.entity.DailyReportLike;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DailyReportLikeRepository extends JpaRepository<DailyReportLike, Long> {

    Optional<DailyReportLike> findByUserIdAndDailyReportId(Long userId, Long dailyReportId);

    boolean existsByUserIdAndDailyReportId(Long userId, Long dailyReportId);

    @Query("""
            select l.dailyReport.id
            from DailyReportLike l
            where l.dailyReport.id in :reportIds
              and l.user.id = :userId
            """)
    List<Long> findLikedReportIds(@Param("reportIds") List<Long> reportIds, @Param("userId") Long userId);

    @Query("""
            select distinct l.dailyReport.id
            from DailyReportLike l
            where l.dailyReport.id in :reportIds
            """)
    List<Long> findReportIdsWithLikes(@Param("reportIds") List<Long> reportIds);

    @Query("""
            select l.user
            from DailyReportLike l
            where l.dailyReport.id = :reportId
              and l.user.id not in :excludedUserIds
              and l.user.deletedAt is null
            order by l.createdAt desc
            """)
    List<User> findLikersByReportId(@Param("reportId") Long reportId, @Param("excludedUserIds") List<Long> excludedUserIds);
}