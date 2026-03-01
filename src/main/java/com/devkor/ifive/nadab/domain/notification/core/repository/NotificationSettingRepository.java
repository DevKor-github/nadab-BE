package com.devkor.ifive.nadab.domain.notification.core.repository;

import com.devkor.ifive.nadab.domain.notification.core.entity.NotificationGroup;
import com.devkor.ifive.nadab.domain.notification.core.entity.NotificationSetting;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

    List<NotificationSetting> findByUser(User user);

    Optional<NotificationSetting> findByUserAndGroup(User user, NotificationGroup group);

    @Query("SELECT ns FROM NotificationSetting ns WHERE ns.user.id IN :userIds")
    List<NotificationSetting> findByUserIdIn(@Param("userIds") List<Long> userIds);

    /**
     * 일일 작성 알림 대상 사용자 조회
     */
    @Query("""
        SELECT ns.user
        FROM NotificationSetting ns
        WHERE ns.group = 'ACTIVITY_REMINDER'
          AND ns.enabled = true
          AND ns.dailyWriteTime = :time
          AND ns.user.deletedAt is null
        """)
    List<User> findUsersForDailyWriteReminder(@Param("time") LocalTime time);
}