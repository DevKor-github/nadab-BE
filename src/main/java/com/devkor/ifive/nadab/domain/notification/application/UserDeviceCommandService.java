package com.devkor.ifive.nadab.domain.notification.application;

import com.devkor.ifive.nadab.domain.notification.core.entity.DevicePlatform;
import com.devkor.ifive.nadab.domain.notification.core.entity.UserDevice;
import com.devkor.ifive.nadab.domain.notification.core.repository.UserDeviceRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserDeviceCommandService {

    private final UserDeviceRepository userDeviceRepository;
    private final UserRepository userRepository;

    /**
     * FCM 토큰 등록
     * - 기존 토큰이 있으면 업데이트, 없으면 새로 생성
     * - 같은 FCM 토큰이 다른 사용자/디바이스에 등록되어 있으면 먼저 삭제 (로그아웃 시 토큰 삭제 실패 대비)
     */

    public boolean registerDevice(
        Long userId,
        String fcmToken,
        String deviceId,
        DevicePlatform platform
    ) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        // 1. 이전 사용자의 토큰이 남아있는 경우 제거 (로그아웃 시 토큰 삭제 실패 대비)
        Optional<UserDevice> existingByToken = userDeviceRepository.findByFcmToken(fcmToken);
        if (existingByToken.isPresent()) {
            UserDevice existing = existingByToken.get();
            // 다른 사용자이거나 다른 디바이스/플랫폼이면 삭제
            if (!existing.getUser().getId().equals(userId)
                || !existing.getDeviceId().equals(deviceId)
                || !existing.getPlatform().equals(platform)) {
                userDeviceRepository.delete(existing);
                log.debug("Deleted existing token: was for userId={}, now for userId={}",
                    existing.getUser().getId(), userId);
            }
        }

        // 2. 기존 디바이스 확인 (user + deviceId + platform)
        Optional<UserDevice> existingDevice = userDeviceRepository
            .findByUserAndDeviceIdAndPlatform(user, deviceId, platform);

        if (existingDevice.isPresent()) {
            // 기존 디바이스 토큰 업데이트
            UserDevice device = existingDevice.get();
            device.updateToken(fcmToken);
            log.debug("Device token updated: userId={}, platform={}", userId, platform);
            return false; // 기존 디바이스 업데이트
        } else {
            // 새 디바이스 등록
            try {
                UserDevice newDevice = UserDevice.create(user, fcmToken, deviceId, platform);
                userDeviceRepository.save(newDevice);
                log.debug("Device registered: userId={}, platform={}", userId, platform);
                return true; // 새 디바이스 등록
            } catch (DataIntegrityViolationException e) {
                // 동시성으로 인해 이미 생성된 경우 (Race Condition)
                log.warn("Race condition detected during device registration: userId={}, deviceId={}, platform={}",
                    userId, deviceId, platform);

                // 다시 조회해서 처리
                Optional<UserDevice> retryDevice = userDeviceRepository
                    .findByUserAndDeviceIdAndPlatform(user, deviceId, platform);

                if (retryDevice.isPresent()) {
                    // Case 1: 이미 생성되었으므로 토큰만 업데이트
                    retryDevice.get().updateToken(fcmToken);
                    log.info("Device token updated after race condition: userId={}, platform={}", userId, platform);
                    return false;
                } else {
                    // Case 2: FCM 토큰 충돌 (다른 사용자가 같은 토큰 사용 중, 이론상 극히 드문 상황)
                    log.error("CRITICAL: FCM token collision detected during race condition! " +
                        "userId={}, deviceId={}, platform={}",
                        userId, deviceId, platform);

                    userDeviceRepository.deleteByFcmToken(fcmToken);
                    UserDevice newDevice = UserDevice.create(user, fcmToken, deviceId, platform);
                    userDeviceRepository.save(newDevice);

                    log.warn("Forcefully registered device after resolving FCM token collision: userId={}, platform={}",
                        userId, platform);
                    return true;
                }
            }
        }
    }

    /**
     * FCM 토큰 삭제
     */
    public void deleteDevice(Long userId, String deviceId, DevicePlatform platform) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        UserDevice device = userDeviceRepository
            .findByUserAndDeviceIdAndPlatform(user, deviceId, platform)
            .orElseThrow(() -> new NotFoundException(ErrorCode.DEVICE_NOT_FOUND));

        userDeviceRepository.delete(device);
        log.debug("Device deleted: userId={}, deviceId={}, platform={}", userId, deviceId, platform);
    }
}