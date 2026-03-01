package com.devkor.ifive.nadab.domain.notification.core.repository;

import com.devkor.ifive.nadab.domain.notification.core.entity.DevicePlatform;
import com.devkor.ifive.nadab.domain.notification.core.entity.UserDevice;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {

    List<UserDevice> findByUser(User user);

    Optional<UserDevice> findByUserAndDeviceIdAndPlatform(User user, String deviceId, DevicePlatform platform);

    Optional<UserDevice> findByFcmToken(String fcmToken);

    @Query("select ud from UserDevice ud join fetch ud.user where ud.user.id in :userIds")
    List<UserDevice> findByUserIdIn(@Param("userIds") List<Long> userIds);

    void deleteByFcmToken(String fcmToken);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from UserDevice ud where ud.fcmToken in :fcmTokens")
    int deleteByFcmTokenIn(@Param("fcmTokens") List<String> fcmTokens);
}