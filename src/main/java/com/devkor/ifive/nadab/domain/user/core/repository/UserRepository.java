package com.devkor.ifive.nadab.domain.user.core.repository;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 조회
    Optional<User> findByEmail(String email);

    // 이메일 중복 체크
    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);
}