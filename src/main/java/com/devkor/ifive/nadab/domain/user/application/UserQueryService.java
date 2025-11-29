package com.devkor.ifive.nadab.domain.user.application;

import com.devkor.ifive.nadab.domain.user.api.dto.response.UserProfileResponse;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.user.infra.ProfileImageUrlBuilder;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.global.shared.util.DateTimeConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepository;
    private final ProfileImageUrlBuilder profileImageUrlBuilder;

    public UserProfileResponse getUserProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. id: " + id));

        return new UserProfileResponse(
                user.getNickname(),
                user.getEmail(),
                profileImageUrlBuilder.buildUserProfileUrl(user),
                DateTimeConverter.convertToSeoulDate(user.getRegisteredAt())
        );
    }
}
