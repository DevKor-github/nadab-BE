package com.devkor.ifive.nadab.domain.auth.application;

import com.devkor.ifive.nadab.domain.auth.api.dto.response.LocalTokenResponse;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.wallet.core.entity.UserWallet;
import com.devkor.ifive.nadab.domain.wallet.core.repository.UserWalletRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.global.security.token.AccessTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class LocalTokenService {

    private final AccessTokenProvider accessTokenProvider;
    private final UserRepository userRepository;
    private final UserWalletRepository userWalletRepository;

    // 로컬 로그인용 토큰 발급
    public LocalTokenResponse issueDummyAccessToken() {
        User user = userRepository.findById(11111L)
                .orElseThrow(() -> new NotFoundException(ErrorCode.AUTH_DUMMY_USER_NOT_FOUND));

        UserWallet wallet = getOrCreateWallet(user);

        List<String> roles = List.of("USER");
        String accessToken = accessTokenProvider.generateToken(user.getId(), roles);

        return new LocalTokenResponse(accessToken);
    }

    public UserWallet getOrCreateWallet(User user) {
        return userWalletRepository.findByUserId(user.getId())
                .orElseGet(() -> userWalletRepository.save(UserWallet.create(user)));
    }

}
