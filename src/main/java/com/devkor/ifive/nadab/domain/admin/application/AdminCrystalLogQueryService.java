package com.devkor.ifive.nadab.domain.admin.application;

import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminCrystalLogResponse;
import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminLogPageResponse;
import com.devkor.ifive.nadab.domain.wallet.core.repository.CrystalLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCrystalLogQueryService {

    private final CrystalLogRepository crystalLogRepository;

    public AdminLogPageResponse<AdminCrystalLogResponse> getLogs(
            AdminLogSearchCondition condition
    ) {
        return AdminLogPageResponse.from(
                crystalLogRepository.findAllForAdmin(
                                condition.nickname(),
                                condition.email(),
                                condition.toPageable()
                        )
                        .map(AdminCrystalLogResponse::from)
        );
    }
}
