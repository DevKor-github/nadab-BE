package com.devkor.ifive.nadab.domain.user.infra;

import com.devkor.ifive.nadab.global.core.config.feature.ReservedNicknamesConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservedNicknameProvider {

    private final ReservedNicknamesConfig reservedNicknamesConfig;

    public boolean isReservedNickname(String nickname) {
        return reservedNicknamesConfig.getNicknames().contains(nickname);
    }
}
