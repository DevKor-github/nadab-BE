package com.devkor.ifive.nadab.domain.admin.application;

import com.devkor.ifive.nadab.domain.admin.api.dto.request.AdminVersionCreateRequest;
import com.devkor.ifive.nadab.domain.appversion.core.entity.AppVersion;
import com.devkor.ifive.nadab.domain.appversion.core.repository.AppVersionRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ConflictException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
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
        appVersionRepository.flush();

        AppVersion appVersion = AppVersion.create(
                request.platform(),
                request.version(),
                request.summary()
        );
        try {
            appVersionRepository.saveAndFlush(appVersion);
            return appVersion.getId();
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(ErrorCode.APP_VERSION_ALREADY_EXISTS);
        }
    }

    public void updateSummary(Long appVersionId, String summary) {
        AppVersion appVersion = appVersionRepository.findById(appVersionId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.APP_VERSION_NOT_FOUND));
        appVersion.updateSummary(summary);
    }
}
