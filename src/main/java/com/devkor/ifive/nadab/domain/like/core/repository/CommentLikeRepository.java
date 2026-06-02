package com.devkor.ifive.nadab.domain.like.core.repository;

import com.devkor.ifive.nadab.domain.like.core.entity.CommentLike;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    Optional<CommentLike> findByUserIdAndCommentId(Long userId, Long commentId);

    boolean existsByUserIdAndCommentId(Long userId, Long commentId);

    @Query("""
            select l.comment.id
            from CommentLike l
            where l.comment.id in :commentIds
              and l.user.id = :userId
            """)
    List<Long> findLikedCommentIds(@Param("commentIds") List<Long> commentIds, @Param("userId") Long userId);

    @Query("""
            select distinct l.comment.id
            from CommentLike l
            where l.comment.id in :commentIds
              and l.user.id not in :excludedUserIds
              and l.user.deletedAt is null
            """)
    List<Long> findCommentIdsWithLikes(
            @Param("commentIds") List<Long> commentIds,
            @Param("excludedUserIds") List<Long> excludedUserIds
    );

    @Query("""
            select l.user
            from CommentLike l
            where l.comment.id = :commentId
              and l.user.id not in :excludedUserIds
              and l.user.deletedAt is null
            order by l.createdAt desc
            """)
    List<User> findLikersByCommentId(@Param("commentId") Long commentId, @Param("excludedUserIds") List<Long> excludedUserIds);
}