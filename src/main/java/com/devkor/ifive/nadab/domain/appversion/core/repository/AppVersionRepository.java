package com.devkor.ifive.nadab.domain.appversion.core.repository;

import com.devkor.ifive.nadab.domain.appversion.core.entity.AppVersion;
import com.devkor.ifive.nadab.domain.appversion.core.entity.AppPlatform;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppVersionRepository extends JpaRepository<AppVersion, Long> {
    List<AppVersion> findByIsLatestTrue();

    Optional<AppVersion> findByPlatformAndIsLatestTrue(AppPlatform platform);

    boolean existsByPlatformAndVersion(AppPlatform platform, String version);
}
