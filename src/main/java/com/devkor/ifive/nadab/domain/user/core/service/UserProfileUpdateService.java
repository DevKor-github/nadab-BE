package com.devkor.ifive.nadab.domain.user.core.service;

import com.devkor.ifive.nadab.domain.user.core.entity.DefaultProfileType;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserProfileUpdateService {

    private final UserRepository userRepository;
    private final ProfileImageService profileImageService;

    public void deleteProfileImage(User user) {
        String oldProfileImageKey = user.getProfileImageKey();

        user.updateToDefaultProfile(DefaultProfileType.DEFAULT);
        profileImageService.deleteProfileImage(oldProfileImageKey);
    }

    public void updateNickname(User user, String nickname) {
        // 자기 자신의 닉네임은 중복 체크에서 제외
        String currentNickname = user.getNickname();
        if (currentNickname == null || !currentNickname.equals(nickname)) {
            if (userRepository.existsByNickname(nickname)) {
                throw new BadRequestException(ErrorCode.NICKNAME_ALREADY_TAKEN);
            }
        }
        user.updateNickname(nickname);
    }

    public void updateProfileImage(User user, String objectKey) {
        profileImageService.checkImageValidity(objectKey);

        String oldProfileImageKey = user.getProfileImageKey();

        user.updateToCustomProfile(objectKey);
        profileImageService.deleteProfileImage(oldProfileImageKey);
    }
}
