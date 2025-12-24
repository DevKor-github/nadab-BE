package com.devkor.ifive.nadab.domain.wallet.application;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLog;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogReason;
import com.devkor.ifive.nadab.domain.wallet.core.entity.UserWallet;
import com.devkor.ifive.nadab.domain.wallet.core.repository.CrystalLogRepository;
import com.devkor.ifive.nadab.domain.wallet.core.repository.UserWalletRepository;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class WalletCommandService {

    private final UserRepository userRepository;
    private final UserWalletRepository userWalletRepository;
    private final CrystalLogRepository crystalLogRepository;

    public long chargeCrystal(Long userId, long amount, CrystalLogReason reason,
                              String refType, Long refId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. id: " + userId));

        UserWallet wallet = userWalletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("지갑을 찾을 수 없습니다. userId: " + user.getId()));

        long balanceAfter = wallet.charge(amount);

        // 같은 트랜잭션 안에서 로그 insert 예약
        CrystalLog log = CrystalLog.create(
                user,
                amount,
                balanceAfter,
                reason,
                refType,
                refId
        );
        crystalLogRepository.save(log);

        // 커밋 시점에 wallet update + log insert가 함께 반영
        return balanceAfter;
    }

    public long consumeCrystal(Long userId, long amount, CrystalLogReason reason,
                              String refType, Long refId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. id: " + userId));

        UserWallet wallet = userWalletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("지갑을 찾을 수 없습니다. userId: " + user.getId()));

        long balanceAfter = wallet.consume(amount);

        CrystalLog log = CrystalLog.create(
                user,
                -amount,
                balanceAfter,
                reason,
                refType,
                refId
        );
        crystalLogRepository.save(log);

        return balanceAfter;
    }
}
