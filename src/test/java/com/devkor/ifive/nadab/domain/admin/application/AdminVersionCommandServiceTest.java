package com.devkor.ifive.nadab.domain.admin.application;

import com.devkor.ifive.nadab.domain.admin.api.dto.request.AdminVersionCreateRequest;
import com.devkor.ifive.nadab.domain.appversion.core.entity.AppPlatform;
import com.devkor.ifive.nadab.domain.appversion.core.entity.AppVersion;
import com.devkor.ifive.nadab.domain.appversion.core.repository.AppVersionRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminVersionCommandServiceTest {

    @Mock
    AppVersionRepository appVersionRepository;

    AdminVersionCommandService adminVersionCommandService;

    @BeforeEach
    void setUp() {
        adminVersionCommandService = new AdminVersionCommandService(appVersionRepository);
    }

    @Test
    void updateVersion_updates_when_new_version_is_greater_than_other_versions() {
        AppVersion target = AppVersion.create(AppPlatform.ANDROID, "1.3.0", "summary");
        when(appVersionRepository.findById(1L)).thenReturn(Optional.of(target));
        when(appVersionRepository.existsByPlatformAndVersionAndIdNot(AppPlatform.ANDROID, "1.10.0", 1L))
                .thenReturn(false);
        when(appVersionRepository.findByPlatformAndIdNot(AppPlatform.ANDROID, 1L))
                .thenReturn(List.of(
                        AppVersion.create(AppPlatform.ANDROID, "1.2.0", "old"),
                        AppVersion.create(AppPlatform.ANDROID, "1.9.0", "old")
                ));

        adminVersionCommandService.updateVersion(1L, "1.10.0");

        assertThat(target.getVersion()).isEqualTo("1.10.0");
        verify(appVersionRepository).flush();
    }

    @Test
    void updateVersion_rejects_version_equal_to_highest_other_version() {
        AppVersion target = AppVersion.create(AppPlatform.IOS, "1.3.0", "summary");
        when(appVersionRepository.findById(1L)).thenReturn(Optional.of(target));
        when(appVersionRepository.existsByPlatformAndVersionAndIdNot(AppPlatform.IOS, "1.2.0", 1L))
                .thenReturn(false);
        when(appVersionRepository.findByPlatformAndIdNot(AppPlatform.IOS, 1L))
                .thenReturn(List.of(AppVersion.create(AppPlatform.IOS, "1.2.0", "old")));

        assertThatThrownBy(() -> adminVersionCommandService.updateVersion(1L, "1.2.0"))
                .isInstanceOfSatisfying(ConflictException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.APP_VERSION_MUST_BE_GREATER_THAN_EXISTING)
                );

        assertThat(target.getVersion()).isEqualTo("1.3.0");
        verify(appVersionRepository, never()).flush();
    }

    @Test
    void updateVersion_rejects_version_lower_than_highest_other_version() {
        AppVersion target = AppVersion.create(AppPlatform.IOS, "1.3.0", "summary");
        when(appVersionRepository.findById(1L)).thenReturn(Optional.of(target));
        when(appVersionRepository.existsByPlatformAndVersionAndIdNot(AppPlatform.IOS, "1.1.2", 1L))
                .thenReturn(false);
        when(appVersionRepository.findByPlatformAndIdNot(AppPlatform.IOS, 1L))
                .thenReturn(List.of(AppVersion.create(AppPlatform.IOS, "1.2.0", "old")));

        assertThatThrownBy(() -> adminVersionCommandService.updateVersion(1L, "1.1.2"))
                .isInstanceOfSatisfying(ConflictException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.APP_VERSION_MUST_BE_GREATER_THAN_EXISTING)
                );

        assertThat(target.getVersion()).isEqualTo("1.3.0");
        verify(appVersionRepository, never()).flush();
    }

    @Test
    void updateVersion_rejects_duplicate_version_before_comparing_order() {
        AppVersion target = AppVersion.create(AppPlatform.ANDROID, "1.3.0", "summary");
        when(appVersionRepository.findById(1L)).thenReturn(Optional.of(target));
        when(appVersionRepository.existsByPlatformAndVersionAndIdNot(AppPlatform.ANDROID, "1.4.0", 1L))
                .thenReturn(true);

        assertThatThrownBy(() -> adminVersionCommandService.updateVersion(1L, "1.4.0"))
                .isInstanceOfSatisfying(ConflictException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.APP_VERSION_ALREADY_EXISTS)
                );

        verify(appVersionRepository, never()).findByPlatformAndIdNot(AppPlatform.ANDROID, 1L);
        verify(appVersionRepository, never()).flush();
    }

    @Test
    void updateVersion_rejects_invalid_version_format() {
        AppVersion target = AppVersion.create(AppPlatform.ANDROID, "1.3.0", "summary");
        when(appVersionRepository.findById(1L)).thenReturn(Optional.of(target));
        when(appVersionRepository.existsByPlatformAndVersionAndIdNot(AppPlatform.ANDROID, "1.4", 1L))
                .thenReturn(false);

        assertThatThrownBy(() -> adminVersionCommandService.updateVersion(1L, "1.4"))
                .isInstanceOfSatisfying(BadRequestException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.APP_VERSION_INVALID_FORMAT)
                );

        verify(appVersionRepository, never()).findByPlatformAndIdNot(AppPlatform.ANDROID, 1L);
        verify(appVersionRepository, never()).flush();
    }

    @Test
    void createVersion_rejects_version_lower_than_existing_platform_version() {
        AdminVersionCreateRequest request = new AdminVersionCreateRequest(
                AppPlatform.ANDROID,
                "1.1.2",
                "summary"
        );
        when(appVersionRepository.existsByPlatformAndVersion(AppPlatform.ANDROID, "1.1.2"))
                .thenReturn(false);
        when(appVersionRepository.findByPlatform(AppPlatform.ANDROID))
                .thenReturn(List.of(AppVersion.create(AppPlatform.ANDROID, "1.2.0", "old")));

        assertThatThrownBy(() -> adminVersionCommandService.createVersion(request))
                .isInstanceOfSatisfying(ConflictException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.APP_VERSION_MUST_BE_GREATER_THAN_EXISTING)
                );

        verify(appVersionRepository, never()).findByPlatformAndIsLatestTrue(AppPlatform.ANDROID);
        verify(appVersionRepository, never()).saveAndFlush(org.mockito.ArgumentMatchers.any());
    }
}
