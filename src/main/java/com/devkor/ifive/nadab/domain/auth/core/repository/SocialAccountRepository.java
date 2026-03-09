package com.devkor.ifive.nadab.domain.auth.core.repository;

import com.devkor.ifive.nadab.domain.auth.core.entity.ProviderType;
import com.devkor.ifive.nadab.domain.auth.core.entity.SocialAccount;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    // 소셜 로그인 사용자 조회 (provider + providerUserId)
    Optional<SocialAccount> findByProviderTypeAndProviderUserId(
            ProviderType providerType,
            String providerUserId
    );

    // 사용자로 소셜 계정 조회
    Optional<SocialAccount> findByUser(User user);

    // 사용자로 소셜 계정 존재 여부 확인
    boolean existsByUser(User user);
}