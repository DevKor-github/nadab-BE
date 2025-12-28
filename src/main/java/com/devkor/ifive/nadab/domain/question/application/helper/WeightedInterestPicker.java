package com.devkor.ifive.nadab.domain.question.application.helper;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 가중치 기반으로 관심사를 선택하는 헬퍼 클래스
 * - 사용자의 기존 관심사에 더 높은 가중치(0.75)를 부여하여 재선택
 * - 나머지 관심사들은 균등하게 나머지 가중치(0.25)를 분배
 */
@Component
public class WeightedInterestPicker {

    public Long pickForReroll(Long userInterestId, List<Long> allInterestIds) {
        if (allInterestIds == null || allInterestIds.isEmpty()) {
            throw new IllegalStateException("interest 목록이 비어있습니다.");
        }
        if (allInterestIds.size() == 1) {
            return allInterestIds.get(0);
        }

        double userWeight = 0.75d;
        double othersWeightEach = 0.25d / (allInterestIds.size() - 1);

        double r = ThreadLocalRandom.current().nextDouble(0.0, 1.0);
        double acc = 0.0;

        for (Long id : allInterestIds) {
            double w = id.equals(userInterestId) ? userWeight : othersWeightEach;
            acc += w;
            if (r <= acc) return id;
        }
        return allInterestIds.get(allInterestIds.size() - 1); // float 오차 방지
    }
}