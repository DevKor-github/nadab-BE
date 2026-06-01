package com.devkor.ifive.nadab.domain.appversion.application;

import com.devkor.ifive.nadab.domain.appversion.core.entity.AppVersion;
import com.devkor.ifive.nadab.domain.appversion.core.entity.UserAppVersionDismissal;
import com.devkor.ifive.nadab.domain.appversion.core.repository.AppVersionRepository;
import com.devkor.ifive.nadab.domain.appversion.core.repository.UserAppVersionDismissalRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AppVersionDismissalCommandService {

    private final UserRepository userRepository;
    private final AppVersionRepository appVersionRepository;
    private final UserAppVersionDismissalRepository userAppVersionDismissalRepository;

    public void dismiss(Long userId, Long appVersionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        AppVersion appVersion = appVersionRepository.findById(appVersionId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.APP_VERSION_NOT_FOUND));

        if (userAppVersionDismissalRepository.existsByUserIdAndAppVersionId(userId, appVersionId)) {
            return;
        }

        try {
            userAppVersionDismissalRepository.save(UserAppVersionDismissal.create(user, appVersion));
        } catch (DataIntegrityViolationException ignored) {
            // Ignore duplicate insert race condition.
        }
    }
}
