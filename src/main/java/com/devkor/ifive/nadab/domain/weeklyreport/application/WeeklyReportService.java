package com.devkor.ifive.nadab.domain.weeklyreport.application;

import com.devkor.ifive.nadab.domain.question.core.repository.DailyQuestionRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.weeklyreport.api.dto.response.WeeklyReportStartResponse;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.WeeklyReportGenerationRequestedEventDto;
import com.devkor.ifive.nadab.domain.weeklyreport.core.dto.WeeklyReserveResultDto;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeeklyReportService {

    private final UserRepository userRepository;
    private final DailyQuestionRepository dailyQuestionRepository; // 네 도메인에 맞게
    private final WeeklyReportTxService weeklyReportTxService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 비동기 시작 API: 즉시 reportId 반환
     */
    public WeeklyReportStartResponse startWeeklyReport(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. id: " + userId));

        // (Tx) Report(PENDING) + reserve consume + log(PENDING)
        WeeklyReserveResultDto reserve = weeklyReportTxService.reserveWeekly(user);

        // 커밋 이후 비동기 실행을 위해 이벤트 발행 (리스너가 AFTER_COMMIT에서 받음)
        eventPublisher.publishEvent(new WeeklyReportGenerationRequestedEventDto(
                reserve.reportId(),
                user.getId(),
                reserve.crystalLogId()
        ));

        return new WeeklyReportStartResponse(reserve.reportId(), "PENDING");
    }
}

