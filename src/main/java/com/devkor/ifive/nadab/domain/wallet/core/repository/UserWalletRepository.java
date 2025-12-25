package com.devkor.ifive.nadab.domain.wallet.core.repository;

import com.devkor.ifive.nadab.domain.wallet.core.entity.UserWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserWalletRepository extends JpaRepository<UserWallet, Long> {

    Optional<UserWallet> findByUserId(Long userId);

    /**
     * 크리스탈을 안전하게 차감(소비)합니다.
     *
     * DB 레벨에서 '현재 잔액 >= 차감할 금액' 조건을 검사하여,
     * 잔액이 충분할 때만 차감을 수행하는 원자적(Atomic) 연산입니다.
     * 이를 통해 어플리케이션 레벨의 조회-수정 시차로 인한 동시성 문제와 마이너스 잔액 발생을 방지합니다.
     *
     * @param userId 차감할 유저의 ID
     * @param amount 차감할 크리스탈 양
     * @return 업데이트된 행의 수 (1이면 차감 성공, 0이면 잔액 부족 또는 유저 없음으로 실패)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE UserWallet w
        SET w.crystalBalance = w.crystalBalance - :amount
        WHERE w.user.id = :userId
          AND w.crystalBalance >= :amount
    """)
    int tryConsume(@Param("userId") Long userId, @Param("amount") long amount);

    /**
     * 사용된 크리스탈을 환불(복구)합니다.
     *
     * 지정된 금액만큼 잔액을 다시 증가시킵니다.
     * 업데이트 후 영속성 컨텍스트를 초기화하여 데이터 정합성을 맞춥니다.
     *
     * @param userId 환불받을 유저의 ID
     * @param amount 환불할 크리스탈 양
     * @return 업데이트된 행의 수
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE UserWallet w
        SET w.crystalBalance = w.crystalBalance + :amount
        WHERE w.user.id = :userId
    """)
    int refund(@Param("userId") Long userId, @Param("amount") long amount);

    @Modifying
    @Query("""
        UPDATE UserWallet w
        SET w.crystalBalance = w.crystalBalance + :amount
        WHERE w.user.id = :userId
    """)
    int charge(@Param("userId") Long userId, @Param("amount") long amount);
}
