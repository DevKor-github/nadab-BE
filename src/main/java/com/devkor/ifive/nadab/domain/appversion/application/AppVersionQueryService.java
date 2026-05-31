package com.devkor.ifive.nadab.domain.appversion.application;

import com.devkor.ifive.nadab.domain.appversion.core.entity.AppPlatform;
import com.devkor.ifive.nadab.domain.appversion.core.entity.AppVersion;
import com.devkor.ifive.nadab.domain.appversion.core.repository.AppVersionRepository;
import com.devkor.ifive.nadab.domain.appversion.core.repository.UserAppVersionDismissalRepository;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.HomeLatestVersionResponse;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.HomePlatformVersionResponse;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.HomeVersionItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppVersionQueryService {

    private final AppVersionRepository appVersionRepository;
    private final UserAppVersionDismissalRepository userAppVersionDismissalRepository;

    public HomeLatestVersionResponse getHomeLatestVersion(Long userId) {
        List<AppVersion> latestVersions = appVersionRepository.findByIsLatestTrue();

        List<Long> appVersionIds = latestVersions.stream()
                .map(AppVersion::getId)
                .toList();

        Set<Long> dismissedAppVersionIds = appVersionIds.isEmpty()
                ? Set.of()
                : userAppVersionDismissalRepository.findDismissedAppVersionIds(userId, appVersionIds).stream()
                .collect(Collectors.toSet());

        Map<AppPlatform, HomePlatformVersionResponse> latestVersionByPlatform = latestVersions.stream()
                .collect(Collectors.toMap(
                        AppVersion::getPlatform,
                        appVersion -> toPlatformResponse(appVersion, dismissedAppVersionIds.contains(appVersion.getId())),
                        (left, right) -> right
                ));

        return new HomeLatestVersionResponse(
                latestVersionByPlatform.get(AppPlatform.IOS),
                latestVersionByPlatform.get(AppPlatform.ANDROID)
        );
    }

    private HomePlatformVersionResponse toPlatformResponse(AppVersion appVersion, boolean dismissed) {
        List<HomeVersionItemResponse> items = Optional.ofNullable(appVersion.getItems())
                .orElse(List.of())
                .stream()
                .map(item -> new HomeVersionItemResponse(item.title(), item.description()))
                .toList();

        return new HomePlatformVersionResponse(
                appVersion.getId(),
                appVersion.getVersion(),
                appVersion.getSummary(),
                items,
                dismissed
        );
    }
}
