package com.devkor.ifive.nadab.domain.admin.application;

import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminVersionHistoryResponse;
import com.devkor.ifive.nadab.domain.appversion.core.entity.AppPlatform;
import com.devkor.ifive.nadab.domain.appversion.core.entity.AppVersion;
import com.devkor.ifive.nadab.domain.appversion.core.entity.AppVersionItem;
import com.devkor.ifive.nadab.domain.appversion.core.repository.AppVersionItemRepository;
import com.devkor.ifive.nadab.domain.appversion.core.repository.AppVersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminVersionQueryServiceTest {

    @Mock
    AppVersionRepository appVersionRepository;

    @Mock
    AppVersionItemRepository appVersionItemRepository;

    AdminVersionQueryService adminVersionQueryService;

    @BeforeEach
    void setUp() {
        adminVersionQueryService = new AdminVersionQueryService(
                appVersionRepository,
                appVersionItemRepository
        );
    }

    @Test
    void getVersionHistory_returns_all_versions_with_items() {
        AppVersion android = appVersion(1L, AppPlatform.ANDROID, "1.2.0", "android summary");
        AppVersion ios = appVersion(2L, AppPlatform.IOS, "1.1.0", "ios summary");
        AppVersionItem item = AppVersionItem.create(android, "Title", "Description", 1);
        ReflectionTestUtils.setField(item, "id", 10L);

        when(appVersionRepository.findAllByOrderByCreatedAtDescIdDesc())
                .thenReturn(List.of(android, ios));
        when(appVersionItemRepository.findByAppVersionIdInOrderByDisplayOrderAsc(List.of(1L, 2L)))
                .thenReturn(List.of(item));

        AdminVersionHistoryResponse response = adminVersionQueryService.getVersionHistory(null);

        assertThat(response.versions()).hasSize(2);
        assertThat(response.versions().get(0).id()).isEqualTo(1L);
        assertThat(response.versions().get(0).items()).hasSize(1);
        assertThat(response.versions().get(0).items().get(0).title()).isEqualTo("Title");
        assertThat(response.versions().get(1).id()).isEqualTo(2L);
        assertThat(response.versions().get(1).items()).isEmpty();
    }

    @Test
    void getVersionHistory_uses_platform_filter_when_platform_exists() {
        AppVersion ios = appVersion(2L, AppPlatform.IOS, "1.1.0", "ios summary");
        when(appVersionRepository.findByPlatformOrderByCreatedAtDescIdDesc(AppPlatform.IOS))
                .thenReturn(List.of(ios));
        when(appVersionItemRepository.findByAppVersionIdInOrderByDisplayOrderAsc(List.of(2L)))
                .thenReturn(List.of());

        AdminVersionHistoryResponse response = adminVersionQueryService.getVersionHistory(AppPlatform.IOS);

        assertThat(response.versions()).hasSize(1);
        assertThat(response.versions().get(0).platform()).isEqualTo(AppPlatform.IOS);
        verify(appVersionRepository, never()).findAllByOrderByCreatedAtDescIdDesc();
    }

    private AppVersion appVersion(Long id, AppPlatform platform, String version, String summary) {
        AppVersion appVersion = AppVersion.create(platform, version, summary);
        ReflectionTestUtils.setField(appVersion, "id", id);
        return appVersion;
    }
}
