package com.example.onlineshop.web;

// AI-assisted: Claude Code, 2026-10; reviewed and modified by 方喆.

import com.example.onlineshop.model.OrderView;
import com.example.onlineshop.repository.MailRepository;
import com.example.onlineshop.repository.UserRepository;
import com.example.onlineshop.service.MailService;
import com.example.onlineshop.service.OrderService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final OrderService orders;
    private final UserRepository users;
    private final MailRepository mails;
    private final MailService mailService;

    public AdminController(OrderService orders, UserRepository users,
                           MailRepository mails, MailService mailService) {
        this.orders = orders;
        this.users = users;
        this.mails = mails;
        this.mailService = mailService;
    }

    @GetMapping({"", "/", "/orders"})
    public String orders(Model model) {
        List<OrderView> views = orders.listAll().stream()
                .map(order -> new OrderView(order,
                        users.findById(order.userId()).orElse(null),
                        mails.findLatestByOrder(order.id()).orElse(null)))
                .toList();
        model.addAttribute("orders", views);
        return "admin/orders";
    }

    @PostMapping("/orders/ship")
    public String ship(@RequestParam long orderId,
                       @RequestParam String trackingNo,
                       RedirectAttributes redirect) {
        try {
            long mailId = orders.ship(orderId, trackingNo);
            mailService.deliver(mailId);
            redirect.addFlashAttribute("message", "订单已发货，通知邮件已处理");
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/orders";
    }

    @PostMapping("/mail/retry")
    public String retry(@RequestParam long mailId, RedirectAttributes redirect) {
        mailService.deliver(mailId);
        redirect.addFlashAttribute("message", "邮件已重新处理");
        return "redirect:/admin/orders";
    }
}