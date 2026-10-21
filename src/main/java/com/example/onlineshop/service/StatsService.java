package com.example.onlineshop.service;

// AI-assisted: Claude Code, 2026-10; reviewed and modified by 方喆.

import com.example.onlineshop.model.DailySales;
import com.example.onlineshop.model.SalesSummary;
import com.example.onlineshop.model.StatsReport;
import com.example.onlineshop.repository.StatsRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class StatsService {

    private final StatsRepository stats;

    public StatsService(StatsRepository stats) {
        this.stats = stats;
    }

    public StatsReport report() {
        SalesSummary summary = stats.summary();

        LocalDate start = LocalDate.now().minusDays(29);
        Map<LocalDate, DailySales> byDay = stats.daily(start).stream()
                .collect(Collectors.toMap(DailySales::day, Function.identity()));

        List<DailySales> daily = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            LocalDate day = start.plusDays(i);
            daily.add(byDay.getOrDefault(day, new DailySales(day, 0, 0)));
        }

        return new StatsReport(summary, daily, stats.topProducts(8), stats.categories());
    }
}