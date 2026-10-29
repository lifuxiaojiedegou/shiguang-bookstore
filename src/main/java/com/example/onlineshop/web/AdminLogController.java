package com.example.onlineshop.web;

// AI-assisted: Claude Code, 2026-10; reviewed and modified by 方喆.

import com.example.onlineshop.model.LogAction;
import com.example.onlineshop.service.LogService;
import java.util.Arrays;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class AdminLogController {

    private final LogService logs;

    public AdminLogController(LogService logs) {
        this.logs = logs;
    }

    @GetMapping("/logs")
    public String logs(@RequestParam(defaultValue = "") String action, Model model) {
        String filter = Arrays.stream(LogAction.values())
                .map(LogAction::name)
                .filter(name -> name.equals(action))
                .findFirst()
                .orElse("");

        model.addAttribute("action", filter);
        model.addAttribute("actions", LogAction.values());
        model.addAttribute("today", logs.countToday());
        model.addAttribute("funnel", logs.productFunnel(10));
        model.addAttribute("keywords", logs.topKeywords(10));
        model.addAttribute("logs", logs.recent(filter, 100));
        return "admin/logs";
    }
}
