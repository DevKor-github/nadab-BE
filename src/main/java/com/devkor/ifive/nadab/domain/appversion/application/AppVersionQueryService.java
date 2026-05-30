package com.devkor.ifive.nadab.domain.appversion.application;

import com.devkor.ifive.nadab.domain.appversion.core.entity.AppPlatform;
import com.devkor.ifive.nadab.domain.appversion.core.entity.AppVersion;
import com.devkor.ifive.nadab.domain.appversion.core.repository.AppVersionRepository;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.HomeLatestVersionResponse;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.HomePlatformVersionResponse;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.HomeVersionItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppVersionQueryService {

    private final AppVersionRepository appVersionRepository;

    public HomeLatestVersionResponse getHomeLatestVersion() {
        Map<AppPlatform, HomePlatformVersionResponse> latestVersionByPlatform = appVersionRepository.findByIsLatestTrue().stream()
                .collect(Collectors.toMap(
                        AppVersion::getPlatform,
                        this::toPlatformResponse,
                        (left, right) -> right
                ));

        return new HomeLatestVersionResponse(
                latestVersionByPlatform.get(AppPlatform.IOS),
                latestVersionByPlatform.get(AppPlatform.ANDROID),
                latestVersionByPlatform.get(AppPlatform.WEB)
        );
    }

    private HomePlatformVersionResponse toPlatformResponse(AppVersion appVersion) {
        List<HomeVersionItemResponse> items = Optional.ofNullable(appVersion.getItems())
                .orElse(List.of())
                .stream()
                .map(item -> new HomeVersionItemResponse(item.title(), item.description()))
                .toList();

        return new HomePlatformVersionResponse(
                appVersion.getVersion(),
                appVersion.getSummary(),
                items
        );
    }
}
