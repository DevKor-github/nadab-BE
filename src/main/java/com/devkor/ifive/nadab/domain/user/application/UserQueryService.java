package com.devkor.ifive.nadab.domain.user.application;

import com.devkor.ifive.nadab.domain.user.api.dto.response.CheckNicknameResponse;
import com.devkor.ifive.nadab.domain.user.api.dto.response.UserProfileResponse;
import com.devkor.ifive.nadab.domain.user.core.dto.NicknameValidationResultDto;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.entity.UserInterest;
import com.devkor.ifive.nadab.domain.user.core.repository.UserInterestRepository;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.user.core.service.NicknameValidator;
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
    private final NicknameValidator nicknameValidator;
    private final UserInterestRepository userInterestRepository;

    public UserProfileResponse getUserProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. id: " + id));

        UserInterest userInterest = userInterestRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("사용자의 관심 주제를 찾을 수 없습니다. id: " + id));

        return new UserProfileResponse(
                user.getNickname(),
                user.getEmail(),
                profileImageUrlBuilder.buildUserProfileUrl(user),
                userInterest.getInterest().getCode().name(),
                DateTimeConverter.convertToSeoulDate(user.getRegisteredAt())
        );
    }

    public CheckNicknameResponse checkNickname(String nickname) {
        boolean isNicknameExists = userRepository.existsByNickname(nickname);

        if (isNicknameExists) {
            return new CheckNicknameResponse(false, "이미 사용 중인 닉네임입니다.");
        }

        NicknameValidationResultDto validationResult =
                nicknameValidator.validateNickname(nickname);

        return new CheckNicknameResponse(
                validationResult.isValid(),
                validationResult.reason()
        );
    }
}
