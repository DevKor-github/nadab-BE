package com.devkor.ifive.nadab.domain.friend.core.repository;

import com.devkor.ifive.nadab.domain.friend.core.entity.Friendship;
import com.devkor.ifive.nadab.domain.friend.core.entity.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    @Query("""
        select case when exists (
            select 1 from Friendship f
            where f.user1.id = :userId1 and f.user2.id = :userId2
              and f.user1.deletedAt is null and f.user2.deletedAt is null
        ) then true else false end
        """)
    boolean existsByUserIds(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("""
        select count(f) from Friendship f
        where (f.user1.id = :userId or f.user2.id = :userId)
        and f.status = :status
        """)
    int countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") FriendshipStatus status);

    @Query("""
        select f from Friendship f
        left join fetch f.user1
        left join fetch f.user2
        where (f.user1.id = :userId or f.user2.id = :userId)
        and f.status = :status
        order by f.createdAt desc
        """)
    List<Friendship> findByUserIdAndStatusWithUsers(@Param("userId") Long userId, @Param("status") FriendshipStatus status);

    @Query("""
        select f from Friendship f
        where (f.user1.id = :userId or f.user2.id = :userId)
        and f.requester.id != :userId
        and f.status = 'PENDING'
        and f.user1.deletedAt is null and f.user2.deletedAt is null
        order by f.createdAt desc
        """)
    List<Friendship> findReceivedPendingRequests(@Param("userId") Long userId);

    @Query("""
        select f from Friendship f
        join fetch f.requester r
        where (f.user1.id = :userId or f.user2.id = :userId)
        and f.requester.id != :userId
        and f.status = 'PENDING'
        and lower(r.nickname) like lower(:keyword) escape '\\'
        and f.user1.deletedAt is null and f.user2.deletedAt is null
        and not exists (
            select 1 from UserBlock ub
            where (ub.blocker.id = :userId and ub.blocked.id = r.id)
               or (ub.blocker.id = r.id and ub.blocked.id = :userId)
        )
        order by f.createdAt desc
        """)
    List<Friendship> findReceivedPendingRequestsByKeyword(
            @Param("userId") Long userId,
            @Param("keyword") String keyword
    );

    @Modifying
    @Query("""
        delete from Friendship f
        where f.user1.id = :userId1
          and f.user2.id = :userId2
    """)
    int deleteByUserIds(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

}
