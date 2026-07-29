package com.devkor.ifive.nadab.domain.auth.application;

import com.devkor.ifive.nadab.domain.auth.api.dto.response.LocalTokenResponse;
import com.devkor.ifive.nadab.domain.auth.infra.LocalDummyUserRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.global.security.token.AccessTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Profile("local")
@Service
@Transactional
@RequiredArgsConstructor
public class LocalTokenService {

    private final AccessTokenProvider accessTokenProvider;
    private final UserRepository userRepository;
    private final LocalDummyUserRepository localDummyUserRepository;

    // 로컬 로그인용 토큰 발급
    public LocalTokenResponse issueDummyAccessToken() {
        Long dummyUserId = localDummyUserRepository.createIfAbsent();
        User user = userRepository.findById(dummyUserId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.AUTH_DUMMY_USER_NOT_FOUND));

        List<String> roles = List.of("USER");
        String accessToken = accessTokenProvider.generateToken(user.getId(), roles);

        return new LocalTokenResponse(accessToken);
    }
}
