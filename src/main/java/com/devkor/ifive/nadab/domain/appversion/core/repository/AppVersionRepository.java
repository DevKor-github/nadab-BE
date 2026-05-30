package com.devkor.ifive.nadab.domain.appversion.core.repository;

import com.devkor.ifive.nadab.domain.appversion.core.entity.AppVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppVersionRepository extends JpaRepository<AppVersion, Long> {
    List<AppVersion> findByIsLatestTrue();
}
