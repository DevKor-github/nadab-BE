package com.devkor.ifive.nadab.domain.moderation.core.repository;

import com.devkor.ifive.nadab.domain.moderation.core.entity.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {

    boolean existsByBlocker_IdAndBlocked_Id(Long blockerId, Long blockedId);

    Optional<UserBlock> findByIdAndBlocker_Id(Long id, Long blockerId);

    @Query("""
        select ub
        from UserBlock ub
        join fetch ub.blocked b
        where ub.blocker.id = :blockerId
          and b.deletedAt is null
        order by ub.createdAt desc
    """)
    List<UserBlock> findByBlockerIdWithBlockedUser(@Param("blockerId") Long blockerId);

    @Query("""
        select case when exists (
            select 1
            from UserBlock ub
            where (ub.blocker.id = :userId and ub.blocked.id = :otherUserId)
               or (ub.blocker.id = :otherUserId and ub.blocked.id = :userId)
        ) then true else false end
    """)
    boolean existsAnyBlockBetweenUsers(@Param("userId") Long userId, @Param("otherUserId") Long otherUserId);
}
