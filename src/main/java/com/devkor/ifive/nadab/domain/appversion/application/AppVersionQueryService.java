package com.devkor.ifive.nadab.domain.appversion.application;

import com.devkor.ifive.nadab.domain.appversion.core.entity.AppPlatform;
import com.devkor.ifive.nadab.domain.appversion.core.entity.AppVersion;
import com.devkor.ifive.nadab.domain.appversion.core.repository.AppVersionRepository;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.HomeLatestVersionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppVersionQueryService {

    private final AppVersionRepository appVersionRepository;

    public HomeLatestVersionResponse getHomeLatestVersion() {
        Map<AppPlatform, String> latestVersionByPlatform = appVersionRepository.findByIsLatestTrue().stream()
                .collect(Collectors.toMap(
                        AppVersion::getPlatform,
                        AppVersion::getVersion,
                        (left, right) -> right
                ));

        return new HomeLatestVersionResponse(
                latestVersionByPlatform.get(AppPlatform.IOS),
                latestVersionByPlatform.get(AppPlatform.ANDROID),
                latestVersionByPlatform.get(AppPlatform.WEB)
        );
    }
}
