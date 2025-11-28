package com.devkor.ifive.nadab.domain.auth.application;

import com.devkor.ifive.nadab.domain.auth.api.dto.response.LocalTokenResponse;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
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

    // 로컬 로그인용 토큰 발급
    public LocalTokenResponse issueDummyAccessToken() {
        User user = userRepository.findById(11111L)
                .orElseThrow(() -> new NotFoundException("더미 유저(ID: 11111)가 존재하지 않습니다."));

        List<String> roles = List.of("USER");
        String accessToken = accessTokenProvider.generateToken(user.getId(), roles);

        return new LocalTokenResponse(accessToken);
    }
}
