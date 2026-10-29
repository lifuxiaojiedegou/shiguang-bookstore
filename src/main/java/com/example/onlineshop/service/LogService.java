package com.example.onlineshop.service;

// AI-assisted: Claude Code, 2026-10; reviewed and modified by 方喆.

import com.example.onlineshop.model.ActivityLog;
import com.example.onlineshop.model.KeywordCount;
import com.example.onlineshop.model.LogAction;
import com.example.onlineshop.model.ProductFunnel;
import com.example.onlineshop.model.ShopUser;
import com.example.onlineshop.repository.LogRepository;
import com.example.onlineshop.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LogService {

    private static final Logger log = LoggerFactory.getLogger(LogService.class);

    private final LogRepository logs;
    private final UserRepository users;

    public LogService(LogRepository logs, UserRepository users) {
        this.logs = logs;
        this.users = users;
    }

    // 日志只是旁路记录：写入失败只打印警告，不影响浏览、加购、下单等业务结果
    public void record(Principal principal, HttpServletRequest request, LogAction action,
                       Long productId, String productName, String detail) {
        try {
            Long userId = principal == null ? null
                    : users.findByEmail(principal.getName()).map(ShopUser::id).orElse(null);
            logs.insert(userId, productId, productName, action.name(),
                    truncate(detail, 200), truncate(request.getRemoteAddr(), 45));
        } catch (RuntimeException exception) {
            log.warn("写入{}日志失败：{}", action.label(), exception.getMessage());
        }
    }

    public List<ActivityLog> recent(String action, int limit) {
        return logs.findRecent(action, limit);
    }

    public Map<LogAction, Long> countToday() {
        return logs.countToday();
    }

    public List<ProductFunnel> productFunnel(int limit) {
        return logs.productFunnel(limit);
    }

    public List<KeywordCount> topKeywords(int limit) {
        return logs.topKeywords(limit);
    }

    private String truncate(String value, int max) {
        return value == null || value.length() <= max ? value : value.substring(0, max);
    }
}
