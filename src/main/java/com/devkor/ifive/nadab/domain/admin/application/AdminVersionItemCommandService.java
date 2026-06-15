package com.devkor.ifive.nadab.domain.admin.application;

import com.devkor.ifive.nadab.domain.admin.api.dto.request.AdminVersionItemUpsertRequest;
import com.devkor.ifive.nadab.domain.appversion.core.entity.AppVersion;
import com.devkor.ifive.nadab.domain.appversion.core.entity.AppVersionItem;
import com.devkor.ifive.nadab.domain.appversion.core.repository.AppVersionItemRepository;
import com.devkor.ifive.nadab.domain.appversion.core.repository.AppVersionRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ConflictException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminVersionItemCommandService {

    private final AppVersionRepository appVersionRepository;
    private final AppVersionItemRepository appVersionItemRepository;

    public Long createItem(Long appVersionId, AdminVersionItemUpsertRequest request) {
        AppVersion appVersion = appVersionRepository.findById(appVersionId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.APP_VERSION_NOT_FOUND));

        if (appVersionItemRepository.existsByAppVersionIdAndDisplayOrder(appVersionId, request.displayOrder())) {
            throw new ConflictException(ErrorCode.APP_VERSION_ITEM_DISPLAY_ORDER_DUPLICATED);
        }

        AppVersionItem item = AppVersionItem.create(
                appVersion,
                request.title(),
                request.description(),
                request.displayOrder()
        );

        try {
            appVersionItemRepository.saveAndFlush(item);
            return item.getId();
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(ErrorCode.APP_VERSION_ITEM_DISPLAY_ORDER_DUPLICATED);
        }
    }

    public void updateItem(Long appVersionItemId, AdminVersionItemUpsertRequest request) {
        AppVersionItem item = appVersionItemRepository.findById(appVersionItemId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.APP_VERSION_ITEM_NOT_FOUND));

        Long appVersionId = item.getAppVersion().getId();
        if (appVersionItemRepository.existsByAppVersionIdAndDisplayOrderAndIdNot(
                appVersionId, request.displayOrder(), appVersionItemId
        )) {
            throw new ConflictException(ErrorCode.APP_VERSION_ITEM_DISPLAY_ORDER_DUPLICATED);
        }

        try {
            item.update(request.title(), request.description(), request.displayOrder());
            appVersionItemRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(ErrorCode.APP_VERSION_ITEM_DISPLAY_ORDER_DUPLICATED);
        }
    }

    public void deleteItem(Long appVersionItemId) {
        AppVersionItem item = appVersionItemRepository.findById(appVersionItemId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.APP_VERSION_ITEM_NOT_FOUND));
        appVersionItemRepository.delete(item);
    }
}
