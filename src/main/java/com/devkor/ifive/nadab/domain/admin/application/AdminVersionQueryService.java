package com.devkor.ifive.nadab.domain.admin.application;

import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminLatestVersionsResponse;
import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminVersionHistoryItemResponse;
import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminVersionHistoryResponse;
import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminVersionItemResponse;
import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminVersionResponse;
import com.devkor.ifive.nadab.domain.appversion.core.entity.AppPlatform;
import com.devkor.ifive.nadab.domain.appversion.core.entity.AppVersion;
import com.devkor.ifive.nadab.domain.appversion.core.entity.AppVersionItem;
import com.devkor.ifive.nadab.domain.appversion.core.repository.AppVersionItemRepository;
import com.devkor.ifive.nadab.domain.appversion.core.repository.AppVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminVersionQueryService {

    private final AppVersionRepository appVersionRepository;
    private final AppVersionItemRepository appVersionItemRepository;

    public AdminLatestVersionsResponse getLatestVersions() {
        List<AppVersion> latestVersions = appVersionRepository.findByIsLatestTrue();
        List<Long> appVersionIds = latestVersions.stream()
                .map(AppVersion::getId)
                .toList();

        List<AppVersionItem> items = appVersionIds.isEmpty()
                ? List.of()
                : appVersionItemRepository.findByAppVersionIdInOrderByDisplayOrderAsc(appVersionIds);

        Map<Long, List<AdminVersionItemResponse>> itemsByVersionId = items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getAppVersion().getId(),
                        Collectors.mapping(
                                item -> new AdminVersionItemResponse(
                                        item.getId(),
                                        item.getTitle(),
                                        item.getDescription(),
                                        item.getDisplayOrder()
                                ),
                                Collectors.toList()
                        )
                ));

        List<AdminVersionResponse> versions = latestVersions.stream()
                .sorted(Comparator.comparing(version -> version.getPlatform().name()))
                .map(version -> new AdminVersionResponse(
                        version.getId(),
                        version.getPlatform(),
                        version.getVersion(),
                        version.getSummary(),
                        itemsByVersionId.getOrDefault(version.getId(), List.of())
                ))
                .toList();

        return new AdminLatestVersionsResponse(versions);
    }

    public AdminVersionHistoryResponse getVersionHistory(AppPlatform platform) {
        List<AppVersion> appVersions = platform == null
                ? appVersionRepository.findAllByOrderByCreatedAtDescIdDesc()
                : appVersionRepository.findByPlatformOrderByCreatedAtDescIdDesc(platform);
        List<Long> appVersionIds = appVersions.stream()
                .map(AppVersion::getId)
                .toList();

        List<AppVersionItem> items = appVersionIds.isEmpty()
                ? List.of()
                : appVersionItemRepository.findByAppVersionIdInOrderByDisplayOrderAsc(appVersionIds);

        Map<Long, List<AdminVersionItemResponse>> itemsByVersionId = items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getAppVersion().getId(),
                        Collectors.mapping(
                                item -> new AdminVersionItemResponse(
                                        item.getId(),
                                        item.getTitle(),
                                        item.getDescription(),
                                        item.getDisplayOrder()
                                ),
                                Collectors.toList()
                        )
                ));

        List<AdminVersionHistoryItemResponse> versions = appVersions.stream()
                .map(version -> new AdminVersionHistoryItemResponse(
                        version.getId(),
                        version.getPlatform(),
                        version.getVersion(),
                        version.getIsLatest(),
                        version.getSummary(),
                        version.getCreatedAt(),
                        version.getUpdatedAt(),
                        itemsByVersionId.getOrDefault(version.getId(), List.of())
                ))
                .toList();

        return new AdminVersionHistoryResponse(versions);
    }
}
