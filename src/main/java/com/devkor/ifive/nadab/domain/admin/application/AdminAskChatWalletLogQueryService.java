package com.devkor.ifive.nadab.domain.admin.application;

import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminAskChatWalletLogResponse;
import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminLogPageResponse;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatWalletLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAskChatWalletLogQueryService {

    private final AskChatWalletLogRepository askChatWalletLogRepository;

    public AdminLogPageResponse<AdminAskChatWalletLogResponse> getLogs(
            AdminLogSearchCondition condition
    ) {
        return AdminLogPageResponse.from(
                askChatWalletLogRepository.findAllForAdmin(
                                condition.nickname(),
                                condition.email(),
                                condition.toPageable()
                        )
                        .map(AdminAskChatWalletLogResponse::from)
        );
    }
}
