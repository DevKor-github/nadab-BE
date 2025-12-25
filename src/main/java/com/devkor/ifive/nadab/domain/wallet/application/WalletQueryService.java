package com.devkor.ifive.nadab.domain.wallet.application;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.wallet.api.dto.response.WalletBalanceResponse;
import com.devkor.ifive.nadab.domain.wallet.core.entity.UserWallet;
import com.devkor.ifive.nadab.domain.wallet.core.repository.UserWalletRepository;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WalletQueryService {

    private final UserRepository userRepository;
    private final UserWalletRepository userWalletRepository;

    public WalletBalanceResponse getWalletBalance(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. id: " + userId));

        UserWallet wallet = userWalletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("지갑을 찾을 수 없습니다. userId: " + user.getId()));

        return new WalletBalanceResponse(wallet.getCrystalBalance());
    }
}
