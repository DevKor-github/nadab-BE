package com.devkor.ifive.nadab.domain.user.core.service;

import com.devkor.ifive.nadab.domain.user.core.dto.NicknameValidationResultDto;
import com.devkor.ifive.nadab.domain.user.infra.ProfanityChecker;
import com.devkor.ifive.nadab.domain.user.infra.ReservedNicknameProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NicknameValidator {

    private final ProfanityChecker profanityChecker;
    private final ReservedNicknameProvider reservedNicknameProvider;

    public NicknameValidationResultDto validateNickname(String nickname) {
        if (nickname == null || nickname.isEmpty()) {
            return NicknameValidationResultDto.fail("닉네임이 비어있습니다.");
        }

        // 앞뒤 공백 존재 여부 체크
        if (!nickname.equals(nickname.trim())) {
            return NicknameValidationResultDto.fail("닉네임 앞뒤에는 공백을 포함할 수 없습니다.");
        }

        // 길이 검사
        int length = nickname.length();
        if (length < 2 || length > 10) {
            return NicknameValidationResultDto.fail("닉네임은 2자 이상 10자 이하여야 합니다.");
        }

        // 한글/영문만 허용
        if (!nickname.matches("^[가-힣a-zA-Z]+$")) {
            return NicknameValidationResultDto.fail("닉네임은 한글과 영문만 사용할 수 있습니다.");
        }

        // 예약어 검사
        if (reservedNicknameProvider.isReservedNickname(nickname)) {
            return NicknameValidationResultDto.fail("사용할 수 없는 닉네임입니다.");
        }

        // 비속어 검사
        if (profanityChecker.containsProfanity(nickname)) {
            return NicknameValidationResultDto.fail("부적절한 단어가 포함되어 있습니다.");
        }

        return NicknameValidationResultDto.ok();
    }
}
