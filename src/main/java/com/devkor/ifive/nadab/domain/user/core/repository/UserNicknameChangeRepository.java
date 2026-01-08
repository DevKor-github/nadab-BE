package com.devkor.ifive.nadab.domain.user.core.repository;

import com.devkor.ifive.nadab.domain.user.core.entity.UserNicknameChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

public interface UserNicknameChangeRepository extends JpaRepository<UserNicknameChange, Long> {

    @Query("""
        select count(h)
        from UserNicknameChange h
        where h.user.id = :userId
          and h.createdAt >= :since
    """)
    long countRecentChanges(@Param("userId") Long userId, @Param("since") OffsetDateTime since);
}

