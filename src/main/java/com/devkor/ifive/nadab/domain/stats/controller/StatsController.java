package com.devkor.ifive.nadab.domain.stats.controller;

import com.devkor.ifive.nadab.domain.stats.application.DailyStatsService;
import com.devkor.ifive.nadab.domain.stats.core.dto.daily.DailyStatsViewModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class StatsController {

    private final DailyStatsService dailyStatsService;

    @GetMapping("stats/daily")
    public String dailyStats(Model model) {
        DailyStatsViewModel vm = dailyStatsService.getDailyStatsLast7Days();
        model.addAttribute("vm", vm);
        model.addAttribute("activeTab", "daily");
        return "stats/daily";
    }

    @GetMapping("/stats/total")
    public String totalStats(Model model) {
        model.addAttribute("activeTab", "total");
        return "stats/total";
    }
}
