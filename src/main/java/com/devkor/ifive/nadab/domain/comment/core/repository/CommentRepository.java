package com.devkor.ifive.nadab.domain.comment.core.repository;

import com.devkor.ifive.nadab.domain.comment.core.dto.SubCommentCountDto;
import com.devkor.ifive.nadab.domain.comment.core.entity.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("""
        select c from Comment c
        join fetch c.author
        join fetch c.dailyReport
        where c.id = :id
          and c.deletedAt is null
    """)
    Optional<Comment> findByIdWithAuthorAndDailyReport(@Param("id") Long id);

    @Query("""
        select c.parentComment.author.id from Comment c
        where c.id = :id
          and c.parentComment is not null
    """)
    Optional<Long> findParentAuthorIdById(@Param("id") Long id);

    @Query("""
        select c from Comment c
        join fetch c.author a
        where c.dailyReport.id = :dailyReportId
          and c.parentComment is null
          and c.deletedAt is null
          and a.deletedAt is null
          and (:cursor is null or c.id < :cursor)
          and a.id not in :excludedUserIds
        order by c.id desc
    """)
    List<Comment> findTopLevelComments(
            @Param("dailyReportId") Long dailyReportId,
            @Param("cursor") Long cursor,
            @Param("excludedUserIds") List<Long> excludedUserIds,
            Pageable pageable
    );

    @Query("""
        select c from Comment c
        join fetch c.author a
        where c.parentComment.id = :parentCommentId
          and c.deletedAt is null
          and a.deletedAt is null
          and (:cursor is null or c.id < :cursor)
          and a.id not in :excludedUserIds
        order by c.id desc
    """)
    List<Comment> findSubComments(
            @Param("parentCommentId") Long parentCommentId,
            @Param("cursor") Long cursor,
            @Param("excludedUserIds") List<Long> excludedUserIds,
            Pageable pageable
    );

    @Query("""
        select new com.devkor.ifive.nadab.domain.comment.core.dto.SubCommentCountDto(
            c.parentComment.id, count(c)
        )
        from Comment c
        where c.parentComment.id in :parentIds
          and c.deletedAt is null
          and c.author.deletedAt is null
          and c.author.id not in :excludedUserIds
          and (
            c.secret = false
            or c.author.id = :currentUserId
            or c.parentComment.id in :visibleSecretParentIds
          )
        group by c.parentComment.id
    """)
    List<SubCommentCountDto> countVisibleSubCommentsByParentIds(
            @Param("parentIds") List<Long> parentIds,
            @Param("excludedUserIds") List<Long> excludedUserIds,
            @Param("currentUserId") Long currentUserId,
            @Param("visibleSecretParentIds") List<Long> visibleSecretParentIds
    );

    @Query("""
        select distinct c.author.id
        from Comment c
        where c.parentComment.id = :parentCommentId
          and c.deletedAt is null
          and c.author.id not in :excludeUserIds
          and c.author.deletedAt is null
    """)
    List<Long> findDistinctSubCommentAuthorIds(
            @Param("parentCommentId") Long parentCommentId,
            @Param("excludeUserIds") List<Long> excludeUserIds
    );

    @Modifying
    @Query("""
        update Comment c
        set c.deletedAt = :now, c.updatedAt = :now
        where c.parentComment.id = :parentId and c.deletedAt is null
    """)
    void softDeleteSubCommentsByParentId(@Param("parentId") Long parentId, @Param("now") OffsetDateTime now);
}