package com.devkor.ifive.nadab.domain.user.core.service;

import com.devkor.ifive.nadab.domain.user.core.entity.DefaultProfileType;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
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
        // 닉네임 유효성 검사 추가 예정
        user.updateNickname(nickname);
    }

    public void updateProfileImage(User user, String objectKey) {
        profileImageService.checkImageValidity(objectKey);

        String oldProfileImageKey = user.getProfileImageKey();

        user.updateToCustomProfile(objectKey);
        profileImageService.deleteProfileImage(oldProfileImageKey);
    }
}
