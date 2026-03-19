package com.devkor.ifive.nadab.domain.stats.controller;

import com.devkor.ifive.nadab.domain.stats.application.DailyStatsService;
import com.devkor.ifive.nadab.domain.stats.application.TotalStatsService;
import com.devkor.ifive.nadab.domain.stats.application.WeeklyStatsService;
import com.devkor.ifive.nadab.domain.stats.core.dto.daily.DailyStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.total.TotalStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.weekly.WeeklyStatsViewModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class StatsController {

    private final DailyStatsService dailyStatsService;
    private final WeeklyStatsService weeklyStatsService;
    private final TotalStatsService totalStatsService;


    @GetMapping("stats/daily")
    public String dailyStats(Model model) {
        DailyStatsViewModel vm = dailyStatsService.getDailyStatsLast7Days();
        model.addAttribute("vm", vm);
        model.addAttribute("activeTab", "daily");
        return "stats/daily";
    }

    @GetMapping("/stats/weekly")
    public String weeklyStats(Model model) {
        WeeklyStatsViewModel vm = weeklyStatsService.getWeeklyStatsLast7Weeks();
        model.addAttribute("vm", vm);
        model.addAttribute("activeTab", "weekly");
        return "stats/weekly";
    }

    @GetMapping("/stats/total")
    public String totalStats(Model model) {
        TotalStatsViewModel vm = totalStatsService.getTotalStats();
        model.addAttribute("vm", vm);
        model.addAttribute("activeTab", "total");
        return "stats/total";
    }
}
