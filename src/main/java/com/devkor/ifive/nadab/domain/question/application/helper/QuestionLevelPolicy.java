package com.devkor.ifive.nadab.domain.question.application.helper;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * 질문 작성 시, 사용자 레벨에 따른 정책을 적용하는 헬퍼 클래스
 * - 신규 사용자(가입 후 7일 이내)는 레벨 1 질문만 작성 가능
 */
@Component
public class QuestionLevelPolicy {

    private static final int NEWBIE_DAYS = 7;
    private static final int NEWBIE_LEVEL_ONLY = 1;

    public Integer levelOnlyFor(User user, OffsetDateTime now) {
        OffsetDateTime registeredAt = user.getRegisteredAt();
        if (registeredAt == null) return null;

        boolean isNewbie = registeredAt.isAfter(now.minusDays(NEWBIE_DAYS));
        return isNewbie ? NEWBIE_LEVEL_ONLY : null;
    }
}
