package com.devkor.ifive.nadab.domain.admin.application;

import com.devkor.ifive.nadab.domain.admin.api.dto.request.AdminVersionCreateRequest;
import com.devkor.ifive.nadab.domain.appversion.core.entity.AppVersion;
import com.devkor.ifive.nadab.domain.appversion.core.repository.AppVersionRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminVersionCommandService {

    private final AppVersionRepository appVersionRepository;

    public Long createVersion(AdminVersionCreateRequest request) {
        if (appVersionRepository.existsByPlatformAndVersion(request.platform(), request.version())) {
            throw new ConflictException(ErrorCode.APP_VERSION_ALREADY_EXISTS);
        }

        appVersionRepository.findByPlatformAndIsLatestTrue(request.platform())
                .ifPresent(AppVersion::markAsNotLatest);

        AppVersion appVersion = AppVersion.create(
                request.platform(),
                request.version(),
                request.summary()
        );
        try {
            appVersionRepository.save(appVersion);
            return appVersion.getId();
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(ErrorCode.APP_VERSION_ALREADY_EXISTS);
        }
    }
}
