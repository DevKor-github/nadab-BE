package com.devkor.ifive.nadab.domain.dailyreport.application;

import com.devkor.ifive.nadab.domain.dailyreport.core.dto.ConfirmDailyAndRewardDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.PrepareDailyResultDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.AiDailyReportResultDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReport;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.Emotion;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.EmotionName;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.DailyReportRepository;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.EmotionRepository;
import com.devkor.ifive.nadab.domain.dailyreport.core.service.AnswerEntryService;
import com.devkor.ifive.nadab.domain.dailyreport.core.service.PendingDailyReportService;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLog;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogReason;
import com.devkor.ifive.nadab.domain.wallet.core.entity.UserWallet;
import com.devkor.ifive.nadab.domain.wallet.core.repository.CrystalLogRepository;
import com.devkor.ifive.nadab.domain.wallet.core.repository.UserWalletRepository;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DailyReportTxService {

    private final UserRepository userRepository;
    private final DailyReportRepository dailyReportRepository;
    private final EmotionRepository emotionRepository;
    private final UserWalletRepository userWalletRepository;
    private final CrystalLogRepository crystalLogRepository;

    private final AnswerEntryService answerEntryService;
    private final PendingDailyReportService pendingDailyReportService;


    private static final long DAILY_REPORT_REWARD = 10L;

    protected PrepareDailyResultDto prepareDaily(User user, DailyQuestion dq, String answerText) {

        //  AnswerEntry 생성 또는 조회 (별도의 트랜잭션)
        AnswerEntry entry = answerEntryService.getOrCreateTodayAnswerEntry(user, dq, answerText);

        // DailyReport PENDING 생성 또는 조회 (별도의 트랜잭션)
        DailyReport report = pendingDailyReportService.getOrCreatePendingDailyReport(entry);

        return new PrepareDailyResultDto(entry, report.getId(), user.getId());
    }

    protected ConfirmDailyAndRewardDto confirmDailyAndReward(PrepareDailyResultDto prep, AiDailyReportResultDto aiResult) {

        Emotion emotion = emotionRepository.findByName(EmotionName.valueOf(aiResult.emotion()))
                .orElseThrow(() -> new NotFoundException("감정 코드를 찾을 수 없습니다: " + aiResult.emotion()));

        // 1) 리포트 완료
        dailyReportRepository.markCompleted(prep.reportId(), aiResult.message(), emotion.getId());

        // 2) 성공 시점에만 보상 충전
        int updated = userWalletRepository.charge(prep.userId(), DAILY_REPORT_REWARD);
        if (updated == 0) {
            // wallet이 없을 수 있는 상황
            throw new NotFoundException("지갑을 찾을 수 없습니다. userId: " + prep.userId());
        }

        UserWallet wallet = userWalletRepository.findByUserId(prep.userId())
                .orElseThrow(() -> new NotFoundException("지갑을 찾을 수 없습니다. userId: " + prep.userId()));

        long balanceAfter = wallet.getCrystalBalance();

        // 3) 보상 로그 기록(확정 상태로 바로 저장)
        User user = userRepository.getReferenceById(prep.userId());
        CrystalLog log = CrystalLog.createConfirmed(
                user,
                +DAILY_REPORT_REWARD,
                balanceAfter,
                CrystalLogReason.DAILY_ANSWER_REWARD,
                "DAILY_REPORT",
                prep.reportId()
        );

        crystalLogRepository.save(log);

        return new ConfirmDailyAndRewardDto(
                emotion,
                wallet.getCrystalBalance()
        );
    }

    protected void failDaily(Long reportId) {
        dailyReportRepository.markFailed(reportId);
        // 무료이므로 환불/로그 없음
    }
}
