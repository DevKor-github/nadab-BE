package com.devkor.ifive.nadab.domain.admin.application;

import com.devkor.ifive.nadab.domain.admin.api.dto.request.AdminVersionCreateRequest;
import com.devkor.ifive.nadab.domain.appversion.core.entity.AppVersion;
import com.devkor.ifive.nadab.domain.appversion.core.repository.AppVersionRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.ConflictException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminVersionCommandService {

    private static final Pattern SEMANTIC_VERSION_PATTERN = Pattern.compile("\\d+\\.\\d+\\.\\d+");

    private final AppVersionRepository appVersionRepository;

    public Long createVersion(AdminVersionCreateRequest request) {
        if (appVersionRepository.existsByPlatformAndVersion(request.platform(), request.version())) {
            throw new ConflictException(ErrorCode.APP_VERSION_ALREADY_EXISTS);
        }

        SemanticVersion newVersion = SemanticVersion.parse(request.version());
        validateVersionGreaterThanOtherVersions(
                newVersion,
                appVersionRepository.findByPlatform(request.platform())
        );

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

    public void updateVersion(Long appVersionId, String version) {
        AppVersion appVersion = appVersionRepository.findById(appVersionId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.APP_VERSION_NOT_FOUND));

        if (appVersionRepository.existsByPlatformAndVersionAndIdNot(
                appVersion.getPlatform(), version, appVersionId
        )) {
            throw new ConflictException(ErrorCode.APP_VERSION_ALREADY_EXISTS);
        }

        SemanticVersion newVersion = SemanticVersion.parse(version);
        validateVersionGreaterThanOtherVersions(
                newVersion,
                appVersionRepository.findByPlatformAndIdNot(appVersion.getPlatform(), appVersionId)
        );

        try {
            appVersion.updateVersion(version);
            appVersionRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(ErrorCode.APP_VERSION_ALREADY_EXISTS);
        }
    }

    private void validateVersionGreaterThanOtherVersions(
            SemanticVersion newVersion,
            List<AppVersion> otherAppVersions
    ) {
        otherAppVersions.stream()
                .map(versionEntity -> SemanticVersion.parse(versionEntity.getVersion()))
                .max(Comparator.naturalOrder())
                .filter(maxVersion -> newVersion.compareTo(maxVersion) <= 0)
                .ifPresent(maxVersion -> {
                    throw new ConflictException(ErrorCode.APP_VERSION_MUST_BE_GREATER_THAN_EXISTING);
                });
    }

    private record SemanticVersion(BigInteger major, BigInteger minor, BigInteger patch)
            implements Comparable<SemanticVersion> {

        private static SemanticVersion parse(String version) {
            if (!SEMANTIC_VERSION_PATTERN.matcher(version).matches()) {
                throw new BadRequestException(ErrorCode.APP_VERSION_INVALID_FORMAT);
            }

            String[] parts = version.split("\\.");
            return new SemanticVersion(
                    new BigInteger(parts[0]),
                    new BigInteger(parts[1]),
                    new BigInteger(parts[2])
            );
        }

        @Override
        public int compareTo(SemanticVersion other) {
            int majorComparison = major.compareTo(other.major);
            if (majorComparison != 0) {
                return majorComparison;
            }

            int minorComparison = minor.compareTo(other.minor);
            if (minorComparison != 0) {
                return minorComparison;
            }

            return patch.compareTo(other.patch);
        }
    }
}
