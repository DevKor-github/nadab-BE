package com.devkor.ifive.nadab.domain.user.core.repository;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 소셜 로그인 사용자 조회
    @Query("SELECT u FROM User u WHERE u.provider = :provider AND u.providerId = :providerId")
    Optional<User> findByProviderAndProviderId(
            @Param("provider") String provider,
            @Param("providerId") String providerId
    );
}