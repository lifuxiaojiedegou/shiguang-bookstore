package com.example.onlineshop.web;

// AI-assisted: Claude Code, 2026-10; reviewed and modified by 方喆.

import com.example.onlineshop.model.CategorySales;
import com.example.onlineshop.model.DailySales;
import com.example.onlineshop.model.ProductSales;
import com.example.onlineshop.model.StatsReport;
import com.example.onlineshop.service.StatsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminStatsController {

    private final StatsService stats;

    public AdminStatsController(StatsService stats) {
        this.stats = stats;
    }

    @GetMapping("/stats")
    public String stats(Model model) {
        StatsReport report = stats.report();
        model.addAttribute("report", report);
        model.addAttribute("dailyLabels", report.daily().stream()
                .map(day -> day.day().toString()).toList());
        model.addAttribute("dailyRevenueCents", report.daily().stream()
                .map(DailySales::revenueCents).toList());
        model.addAttribute("productLabels", report.topProducts().stream()
                .map(ProductSales::name).toList());
        model.addAttribute("productQuantities", report.topProducts().stream()
                .map(ProductSales::quantity).toList());
        model.addAttribute("categoryLabels", report.categories().stream()
                .map(CategorySales::category).toList());
        model.addAttribute("categoryRevenueCents", report.categories().stream()
                .map(CategorySales::revenueCents).toList());
        return "admin/stats";
    }
}