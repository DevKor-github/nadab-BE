package com.devkor.ifive.nadab.domain.auth.application;

import com.devkor.ifive.nadab.domain.auth.api.dto.response.LocalTokenResponse;
import com.devkor.ifive.nadab.domain.auth.infra.LocalDummyUserRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.security.token.AccessTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalTokenServiceTest {

    @Mock
    private AccessTokenProvider accessTokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LocalDummyUserRepository localDummyUserRepository;

    @InjectMocks
    private LocalTokenService localTokenService;

    @Test
    void issueDummyAccessToken_creates_dummy_user_if_absent_and_returns_access_token() {
        User dummyUser = User.createUser("test@example.com", "hashed_pw");
        ReflectionTestUtils.setField(dummyUser, "id", 11111L);
        when(localDummyUserRepository.createIfAbsent()).thenReturn(11111L);
        when(userRepository.findById(11111L)).thenReturn(Optional.of(dummyUser));
        when(accessTokenProvider.generateToken(11111L, List.of("USER"))).thenReturn("access-token");

        LocalTokenResponse response = localTokenService.issueDummyAccessToken();

        assertThat(response.accessToken()).isEqualTo("access-token");
        verify(localDummyUserRepository).createIfAbsent();
        verify(accessTokenProvider).generateToken(11111L, List.of("USER"));
    }
}
