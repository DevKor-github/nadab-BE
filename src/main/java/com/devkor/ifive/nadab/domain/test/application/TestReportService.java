package com.devkor.ifive.nadab.domain.test.application;

import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReport;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlyReportRepository;
import com.devkor.ifive.nadab.domain.test.api.dto.request.PromptTestDailyReportRequest;
import com.devkor.ifive.nadab.domain.test.api.dto.request.TestDailyReportRequest;
import com.devkor.ifive.nadab.domain.test.api.dto.response.TestDailyReportResponse;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.AiDailyReportResultDto;
import com.devkor.ifive.nadab.domain.test.core.repository.TestCrystalLogRepository;
import com.devkor.ifive.nadab.domain.typereport.core.entity.TypeReport;
import com.devkor.ifive.nadab.domain.typereport.core.entity.TypeReportStatus;
import com.devkor.ifive.nadab.domain.typereport.core.repository.TypeReportRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLog;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogReason;
import com.devkor.ifive.nadab.domain.wallet.core.entity.CrystalLogStatus;
import com.devkor.ifive.nadab.domain.wallet.core.entity.UserWallet;
import com.devkor.ifive.nadab.domain.wallet.core.repository.UserWalletRepository;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReport;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReportStatus;
import com.devkor.ifive.nadab.domain.weeklyreport.core.repository.WeeklyReportRepository;
import com.devkor.ifive.nadab.global.core.prompt.daily.DailyReportPromptLoader;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.global.exception.ai.AiResponseParseException;
import com.devkor.ifive.nadab.global.exception.ai.AiServiceUnavailableException;
import com.devkor.ifive.nadab.global.infra.llm.LlmProvider;
import com.devkor.ifive.nadab.global.infra.llm.LlmRouter;
import com.devkor.ifive.nadab.global.shared.util.MonthRangeCalculator;
import com.devkor.ifive.nadab.global.shared.util.WeekRangeCalculator;
import com.devkor.ifive.nadab.global.shared.util.dto.MonthRangeDto;
import com.devkor.ifive.nadab.global.shared.util.dto.WeekRangeDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TestReportService {

    private final ObjectMapper objectMapper;
    private final DailyReportPromptLoader dailyReportPromptLoader;

    private final LlmRouter llmRouter;

    private final LlmProvider provider = LlmProvider.OPENAI;

    private final UserRepository userRepository;
    private final WeeklyReportRepository weeklyReportRepository;
    private final MonthlyReportRepository monthlyReportRepository;
    private final TypeReportRepository typeReportRepository;
    private final TestCrystalLogRepository testCrystalLogRepository;
    private final UserWalletRepository userWalletRepository;

    private static final long WEEKLY_REPORT_COST = 20L;
    private static final long MONTHLY_REPORT_COST = 40L;
    private static final long TYPE_REPORT_COST = 100L;

    @Transactional
    public TestDailyReportResponse generateTestDailyReport(TestDailyReportRequest request) {
        String question = request.question();
        String answer = request.answer();
        String prompt = dailyReportPromptLoader.loadPrompt()
                .replace("{question}", question)
                .replace("{answer}", answer);

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .temperature(0.3)
                .maxTokens(512)
                .build();

        ChatClient chatClient = llmRouter.route(provider);

        // ChatClient를 통해 GPT API 호출
        String response = chatClient.prompt()
                .user(prompt)
                .options(options)
                .call()
                .content();

        if (response == null || response.trim().isEmpty()) {
            throw new AiServiceUnavailableException(ErrorCode.AI_NO_RESPONSE);
        }

        try {
            // 3. JSON → DTO 역직렬화
            AiDailyReportResultDto result = objectMapper.readValue(response, AiDailyReportResultDto.class);

            String message = result.message();
            String emotion = result.emotion();

            return new TestDailyReportResponse(
                    message,
                    emotion,
                    message.length()
            );

        } catch (Exception e) {
            // GPT가 JSON 형식을 지키지 못했을 경우 대비
            throw new AiResponseParseException(ErrorCode.AI_RESPONSE_PARSE_FAILED);
        }
    }

    @Transactional
    public TestDailyReportResponse generateTestDailyReportWithPrompt(PromptTestDailyReportRequest request, String promptInput) {
        String question = request.question();
        String answer = request.answer();
        String prompt = promptInput
                .replace("{question}", question)
                .replace("{answer}", answer);

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .temperature(
                        request.temperature() != null ? request.temperature() : 0.0
                )
                .maxTokens(512)
                .build();

        ChatClient chatClient = llmRouter.route(provider);

        // ChatClient를 통해 GPT API 호출
        String response = chatClient.prompt()
                .user(prompt)
                .options(options)
                .call()
                .content();

        if (response == null || response.trim().isEmpty()) {
            throw new AiServiceUnavailableException(ErrorCode.AI_NO_RESPONSE);
        }

        try {
            // 3. JSON → DTO 역직렬화
            AiDailyReportResultDto result = objectMapper.readValue(response, AiDailyReportResultDto.class);

            String message = result.message();
            String emotion = result.emotion();

            return new TestDailyReportResponse(
                    message,
                    emotion,
                    message.length()
            );

        } catch (Exception e) {
            // GPT가 JSON 형식을 지키지 못했을 경우 대비
            throw new AiResponseParseException(ErrorCode.AI_RESPONSE_PARSE_FAILED);
        }
    }

    @Transactional
    public void deleteThisWeekWeeklyReport(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        WeekRangeDto range = WeekRangeCalculator.getLastWeekRange();

        WeeklyReport report = weeklyReportRepository.findByUserAndWeekStartDate(user, range.weekStartDate())
                .orElseThrow(() -> new NotFoundException(ErrorCode.WEEKLY_REPORT_NOT_FOUND));

        if (report.getStatus() != WeeklyReportStatus.COMPLETED) {
            throw new BadRequestException(ErrorCode.WEEKLY_REPORT_NOT_COMPLETED);
        }

        CrystalLog purchaseLog = testCrystalLogRepository
                .findByUserIdAndRefTypeAndRefIdAndReasonAndStatus(
                        userId,
                        "WEEKLY_REPORT",
                        report.getId(),
                        CrystalLogReason.REPORT_GENERATE_WEEKLY,
                        CrystalLogStatus.CONFIRMED
                )
                .orElseThrow(() -> new BadRequestException(ErrorCode.CRYSTAL_LOG_NOT_FOUND));

        int updated = userWalletRepository.refund(user.getId(), WEEKLY_REPORT_COST);
        if (updated == 0) {
            throw new NotFoundException(ErrorCode.WALLET_NOT_FOUND);
        }

        UserWallet wallet = userWalletRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.WALLET_NOT_FOUND));
        long balanceAfter = wallet.getCrystalBalance();


        CrystalLog refundLog = CrystalLog.createConfirmed(user, WEEKLY_REPORT_COST, balanceAfter,
                CrystalLogReason.TEST_DELETE_REPORT_REFUND_WEEKLY, "WEEKLY_REPORT_REFUND", report.getId());
        testCrystalLogRepository.save(refundLog);

        testCrystalLogRepository.markRefunded(purchaseLog.getId());

        weeklyReportRepository.delete(report);
    }

    @Transactional
    public void deleteThisMonthMonthlyReport(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        MonthRangeDto range = MonthRangeCalculator.getLastMonthRange();

        MonthlyReport report = monthlyReportRepository.findByUserIdAndMonthStartDate(user.getId(), range.monthStartDate())
                .orElseThrow(() -> new NotFoundException(ErrorCode.MONTHLY_REPORT_NOT_FOUND));

        if (report.getStatus() != MonthlyReportStatus.COMPLETED) {
            throw new BadRequestException(ErrorCode.MONTHLY_REPORT_NOT_COMPLETED);
        }

        CrystalLog purchaseLog = testCrystalLogRepository
                .findByUserIdAndRefTypeAndRefIdAndReasonAndStatus(
                        userId,
                        "MONTHLY_REPORT",
                        report.getId(),
                        CrystalLogReason.REPORT_GENERATE_MONTHLY,
                        CrystalLogStatus.CONFIRMED
                )
                .orElseThrow(() -> new BadRequestException(ErrorCode.CRYSTAL_LOG_NOT_FOUND));

        int updated = userWalletRepository.refund(user.getId(), MONTHLY_REPORT_COST);
        if (updated == 0) {
            throw new NotFoundException(ErrorCode.WALLET_NOT_FOUND);
        }

        UserWallet wallet = userWalletRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.WALLET_NOT_FOUND));
        long balanceAfter = wallet.getCrystalBalance();


        CrystalLog refundLog = CrystalLog.createConfirmed(user, MONTHLY_REPORT_COST, balanceAfter,
                CrystalLogReason.TEST_DELETE_REPORT_REFUND_MONTHLY, "MONTHLY_REPORT_REFUND", report.getId());
        testCrystalLogRepository.save(refundLog);

        testCrystalLogRepository.markRefunded(purchaseLog.getId());

        monthlyReportRepository.delete(report);
    }

    @Transactional
    public void deleteTypeReport(Long userId, String interestCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        InterestCode code = InterestCode.fromString(interestCode);

        TypeReport report = typeReportRepository.findByUserIdAndInterestCodeAndStatusAndDeletedAtIsNull(user.getId(), code, TypeReportStatus.COMPLETED)
                .orElseThrow(() -> new NotFoundException(ErrorCode.TYPE_REPORT_NOT_FOUND));

        if (report.getStatus() != TypeReportStatus.COMPLETED) {
            throw new BadRequestException(ErrorCode.TYPE_REPORT_NOT_COMPLETED);
        }

        CrystalLog purchaseLog = testCrystalLogRepository
                .findByUserIdAndRefTypeAndRefIdAndReasonAndStatus(
                        userId,
                        "TYPE_REPORT: " + code.name(),
                        report.getId(),
                        CrystalLogReason.REPORT_GENERATE_TYPE,
                        CrystalLogStatus.CONFIRMED
                )
                .orElseThrow(() -> new BadRequestException(ErrorCode.CRYSTAL_LOG_NOT_FOUND));

        int updated = userWalletRepository.refund(user.getId(), TYPE_REPORT_COST);
        if (updated == 0) {
            throw new NotFoundException(ErrorCode.WALLET_NOT_FOUND);
        }

        UserWallet wallet = userWalletRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.WALLET_NOT_FOUND));
        long balanceAfter = wallet.getCrystalBalance();

        CrystalLog refundLog = CrystalLog.createConfirmed(user, TYPE_REPORT_COST, balanceAfter,
                CrystalLogReason.TEST_DELETE_REPORT_REFUND_TYPE, "TYPE_REPORT_REFUND", report.getId());
        testCrystalLogRepository.save(refundLog);

        testCrystalLogRepository.markRefunded(purchaseLog.getId());

        typeReportRepository.delete(report);
    }
}
