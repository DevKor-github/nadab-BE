package com.devkor.ifive.nadab.domain.ads.core.repository;

import com.devkor.ifive.nadab.domain.ads.core.entity.AdRewardEvent;
import com.devkor.ifive.nadab.domain.ads.core.entity.AdRewardLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdRewardLogRepository extends JpaRepository<AdRewardLog, Long> {

    // 세션당 특정 이벤트 로그 존재 여부(일시 오류를 세션당 1건만 남기기 위한 멱등 체크)
    boolean existsBySessionIdAndEvent(Long sessionId, AdRewardEvent event);
}