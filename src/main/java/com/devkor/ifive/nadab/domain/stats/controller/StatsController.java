package com.devkor.ifive.nadab.domain.stats.controller;

import com.devkor.ifive.nadab.domain.stats.application.DailyStatsService;
import com.devkor.ifive.nadab.domain.stats.application.MonthlyStatsService;
import com.devkor.ifive.nadab.domain.stats.application.QuestionStatsService;
import com.devkor.ifive.nadab.domain.stats.application.TotalStatsService;
import com.devkor.ifive.nadab.domain.stats.application.TypeStatsService;
import com.devkor.ifive.nadab.domain.stats.application.WithdrawalStatsService;
import com.devkor.ifive.nadab.domain.stats.application.WeeklyStatsService;
import com.devkor.ifive.nadab.domain.stats.application.helper.DailyQuestionOverviewCsvExporter;
import com.devkor.ifive.nadab.domain.stats.application.helper.StatsPeriodResolver;
import com.devkor.ifive.nadab.domain.stats.core.dto.daily.DailyStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.monthly.MonthlyStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionOverviewQuery;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionOverviewSort;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionOverviewSortDirection;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionOverviewViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.total.TotalStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.type.TypeStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.withdrawal.WithdrawalStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.weekly.WeeklyStatsViewModel;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class StatsController {

    private static final MediaType CSV_MEDIA_TYPE = new MediaType("text", "csv", StandardCharsets.UTF_8);
    private static final String QUESTION_OVERVIEW_CSV_FILENAME = "nadab_daily_question_stats.csv";

    private final DailyStatsService dailyStatsService;
    private final WeeklyStatsService weeklyStatsService;
    private final MonthlyStatsService monthlyStatsService;
    private final TotalStatsService totalStatsService;
    private final TypeStatsService typeStatsService;
    private final QuestionStatsService questionStatsService;
    private final WithdrawalStatsService withdrawalStatsService;
    private final DailyQuestionOverviewCsvExporter dailyQuestionOverviewCsvExporter;


    @GetMapping("/stats/daily")
    public String dailyStats(
            @RequestParam(required = false) String date,
            Model model
    ) {
        LocalDate selectedDate = StatsPeriodResolver.resolveDaily(date);
        DailyStatsViewModel vm = dailyStatsService.getDailyStats(selectedDate);
        model.addAttribute("vm", vm);
        model.addAttribute("activeTab", "daily");
        return "stats/daily";
    }

    @GetMapping("/stats/weekly")
    public String weeklyStats(
            @RequestParam(required = false) String week,
            Model model
    ) {
        LocalDate selectedWeekStart = StatsPeriodResolver.resolveWeekly(week);
        WeeklyStatsViewModel vm = weeklyStatsService.getWeeklyStats(selectedWeekStart);
        model.addAttribute("vm", vm);
        model.addAttribute("activeTab", "weekly");
        return "stats/weekly";
    }

    @GetMapping("/stats/monthly")
    public String monthlyStats(
            @RequestParam(required = false) String month,
            Model model
    ) {
        YearMonth selectedMonth = StatsPeriodResolver.resolveMonthly(month);
        MonthlyStatsViewModel vm = monthlyStatsService.getMonthlyStats(selectedMonth);
        model.addAttribute("vm", vm);
        model.addAttribute("activeTab", "monthly");
        return "stats/monthly";
    }

    @GetMapping("/stats/total")
    public String totalStats(Model model) {
        TotalStatsViewModel vm = totalStatsService.getTotalStats();
        model.addAttribute("vm", vm);
        model.addAttribute("activeTab", "total");
        return "stats/total";
    }

    @GetMapping("/stats/type")
    public String typeStats(Model model) {
        TypeStatsViewModel vm = typeStatsService.getTypeStats();
        model.addAttribute("vm", vm);
        model.addAttribute("activeTab", "type");
        return "stats/type";
    }

    @GetMapping("/stats/question")
    public String questionStats(
            @RequestParam(required = false) Long questionId,
            Model model
    ) {
        DailyQuestionStatsViewModel vm = questionStatsService.getQuestionStats(questionId);
        model.addAttribute("vm", vm);
        model.addAttribute("requestedQuestionId", questionId);
        model.addAttribute("activeTab", "question");
        return "stats/question";
    }

    @GetMapping("/stats/question/overview")
    public String questionOverview(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) InterestCode interestCode,
            @RequestParam(required = false) Integer questionLevel,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") long minimumCurrentExposureCount,
            @RequestParam(defaultValue = "CURRENT_EXPOSURE_COUNT") DailyQuestionOverviewSort sort,
            @RequestParam(defaultValue = "DESC") DailyQuestionOverviewSortDirection direction,
            Model model
    ) {
        DailyQuestionOverviewQuery query = resolveQuestionOverviewQuery(
                keyword,
                interestCode,
                questionLevel,
                active,
                minimumCurrentExposureCount,
                sort,
                direction
        );
        DailyQuestionOverviewViewModel vm = questionStatsService.getQuestionOverview(query);
        model.addAttribute("vm", vm);
        model.addAttribute("interestCodes", InterestCode.values());
        model.addAttribute("questionLevels", List.of(1, 2, 3, 4, 5));
        model.addAttribute("activeTab", "question");
        return "stats/question-overview";
    }

    @GetMapping(value = "/stats/question/overview.csv", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<byte[]> questionOverviewCsv(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) InterestCode interestCode,
            @RequestParam(required = false) Integer questionLevel,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") long minimumCurrentExposureCount,
            @RequestParam(defaultValue = "CURRENT_EXPOSURE_COUNT") DailyQuestionOverviewSort sort,
            @RequestParam(defaultValue = "DESC") DailyQuestionOverviewSortDirection direction
    ) {
        DailyQuestionOverviewQuery query = resolveQuestionOverviewQuery(
                keyword,
                interestCode,
                questionLevel,
                active,
                minimumCurrentExposureCount,
                sort,
                direction
        );
        DailyQuestionOverviewViewModel vm = questionStatsService.getQuestionOverview(query);
        return ResponseEntity.ok()
                .contentType(CSV_MEDIA_TYPE)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"%s\"".formatted(QUESTION_OVERVIEW_CSV_FILENAME)
                )
                .body(dailyQuestionOverviewCsvExporter.export(vm));
    }

    private DailyQuestionOverviewQuery resolveQuestionOverviewQuery(
            String keyword,
            InterestCode interestCode,
            Integer questionLevel,
            Boolean active,
            long minimumCurrentExposureCount,
            DailyQuestionOverviewSort sort,
            DailyQuestionOverviewSortDirection direction
    ) {
        if ((questionLevel != null && (questionLevel < 1 || questionLevel > 5))
                || minimumCurrentExposureCount < 0L) {
            throw new BadRequestException(ErrorCode.VALIDATION_FAILED);
        }

        return new DailyQuestionOverviewQuery(
                keyword,
                interestCode,
                questionLevel,
                active,
                minimumCurrentExposureCount,
                sort,
                direction
        );
    }

    @GetMapping("/stats/withdrawal")
    public String withdrawalStats(Model model) {
        WithdrawalStatsViewModel vm = withdrawalStatsService.getWithdrawalStats();
        model.addAttribute("vm", vm);
        model.addAttribute("activeTab", "withdrawal");
        return "stats/withdrawal";
    }
}
