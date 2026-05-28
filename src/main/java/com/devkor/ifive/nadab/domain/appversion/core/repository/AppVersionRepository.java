package com.devkor.ifive.nadab.domain.appversion.core.repository;

import com.devkor.ifive.nadab.domain.appversion.core.entity.AppVersion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppVersionRepository extends JpaRepository<AppVersion, Long> {
}
