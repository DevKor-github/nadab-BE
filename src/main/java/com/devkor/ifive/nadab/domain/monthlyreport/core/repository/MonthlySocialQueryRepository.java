package com.devkor.ifive.nadab.domain.monthlyreport.core.repository;

import com.devkor.ifive.nadab.domain.like.core.entity.DailyReportLike;
import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlySocialInteractionCountDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface MonthlySocialQueryRepository extends Repository<DailyReportLike, Long> {

    @Query("""
        select new com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlySocialInteractionCountDto(
            liker.id,
            liker.nickname,
            liker.profileImageKey,
            liker.defaultProfileType,
            count(l)
        )
        from DailyReportLike l
        join l.user liker
        where l.dailyReport.answerEntry.user.id = :userId
          and l.createdAt >= :startAt
          and l.createdAt < :endAt
          and liker.deletedAt is null
          and exists (
              select 1
              from Friendship f
              where f.status = com.devkor.ifive.nadab.domain.friend.core.entity.FriendshipStatus.ACCEPTED
                and ((f.user1.id = :userId and f.user2.id = liker.id)
                  or (f.user2.id = :userId and f.user1.id = liker.id))
          )
          and not exists (
              select 1
              from UserBlock ub
              where (ub.blocker.id = :userId and ub.blocked.id = liker.id)
                 or (ub.blocker.id = liker.id and ub.blocked.id = :userId)
          )
        group by liker.id, liker.nickname, liker.profileImageKey, liker.defaultProfileType
        """)
    List<MonthlySocialInteractionCountDto> countReceivedLikesByFriend(
            @Param("userId") Long userId,
            @Param("startAt") OffsetDateTime startAt,
            @Param("endAt") OffsetDateTime endAt
    );

    @Query("""
        select new com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlySocialInteractionCountDto(
            author.id,
            author.nickname,
            author.profileImageKey,
            author.defaultProfileType,
            count(c)
        )
        from Comment c
        join c.author author
        where c.dailyReport.answerEntry.user.id = :userId
          and c.createdAt >= :startAt
          and c.createdAt < :endAt
          and c.deletedAt is null
          and author.deletedAt is null
          and exists (
              select 1
              from Friendship f
              where f.status = com.devkor.ifive.nadab.domain.friend.core.entity.FriendshipStatus.ACCEPTED
                and ((f.user1.id = :userId and f.user2.id = author.id)
                  or (f.user2.id = :userId and f.user1.id = author.id))
          )
          and not exists (
              select 1
              from UserBlock ub
              where (ub.blocker.id = :userId and ub.blocked.id = author.id)
                 or (ub.blocker.id = author.id and ub.blocked.id = :userId)
          )
        group by author.id, author.nickname, author.profileImageKey, author.defaultProfileType
        """)
    List<MonthlySocialInteractionCountDto> countReceivedCommentsByFriend(
            @Param("userId") Long userId,
            @Param("startAt") OffsetDateTime startAt,
            @Param("endAt") OffsetDateTime endAt
    );
}
