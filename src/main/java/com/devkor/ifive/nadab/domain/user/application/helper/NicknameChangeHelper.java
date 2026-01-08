package com.devkor.ifive.nadab.domain.user.application.helper;

import com.devkor.ifive.nadab.domain.user.core.repository.UserNicknameChangeRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class NicknameChangeHelper {

    private static final int LIMIT = 2;
    private static final int WINDOW_DAYS = 14;

    private final UserNicknameChangeRepository nicknameChangeRepository;

    public void validateChangeAllowed(Long userId) {

        OffsetDateTime since = OffsetDateTime.now().minusDays(WINDOW_DAYS);

        long recentCount = nicknameChangeRepository.countRecentChanges(userId, since);

        if (recentCount >= LIMIT) {
            throw new BadRequestException(ErrorCode.NICKNAME_CHANGE_LIMIT_EXCEEDED);
        }
    }
}