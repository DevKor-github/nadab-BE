package com.devkor.ifive.nadab.domain.appversion.core.repository;

import com.devkor.ifive.nadab.domain.appversion.core.entity.AppVersionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppVersionItemRepository extends JpaRepository<AppVersionItem, Long> {
    List<AppVersionItem> findByAppVersionIdInOrderByDisplayOrderAsc(List<Long> appVersionIds);

    boolean existsByAppVersionIdAndDisplayOrder(Long appVersionId, Integer displayOrder);

    boolean existsByAppVersionIdAndDisplayOrderAndIdNot(Long appVersionId, Integer displayOrder, Long id);
}
