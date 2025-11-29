package com.devkor.ifive.nadab.domain.user.core.service;

import com.devkor.ifive.nadab.domain.user.core.entity.DefaultProfileType;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserProfileUpdateService {

    private final UserRepository userRepository;
    private final ProfileImageService profileImageService;

    public void deleteProfileImage(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. id: " + id));
        String oldProfileImageKey = user.getProfileImageKey();

        user.updateToDefaultProfile(DefaultProfileType.DEFAULT);
        profileImageService.deleteProfileImage(oldProfileImageKey);
    }
}
