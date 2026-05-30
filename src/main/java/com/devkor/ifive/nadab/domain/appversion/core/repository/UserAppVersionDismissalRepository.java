package com.devkor.ifive.nadab.domain.appversion.core.repository;

import com.devkor.ifive.nadab.domain.appversion.core.entity.UserAppVersionDismissal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserAppVersionDismissalRepository extends JpaRepository<UserAppVersionDismissal, Long> {

    boolean existsByUserIdAndAppVersionId(Long userId, Long appVersionId);

    @Query("""
        select d.appVersion.id
        from UserAppVersionDismissal d
        where d.user.id = :userId
          and d.appVersion.id in :appVersionIds
        """)
    List<Long> findDismissedAppVersionIds(
            @Param("userId") Long userId,
            @Param("appVersionIds") List<Long> appVersionIds
    );
}
