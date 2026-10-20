package com.example.onlineshop.web;

import com.example.onlineshop.model.CartItem;
import com.example.onlineshop.repository.UserRepository;
import com.example.onlineshop.service.CartService;
import com.example.onlineshop.service.OrderService;
import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrderController {

    private final OrderService orders;
    private final CartService carts;
    private final UserRepository users;

    public OrderController(OrderService orders, CartService carts, UserRepository users) {
        this.orders = orders;
        this.carts = carts;
        this.users = users;
    }

    @GetMapping("/checkout")
    public String checkoutForm(Principal principal, HttpSession session, Model model) {
        long userId = currentUserId(principal);
        List<CartItem> items = carts.list(userId);
        if (items.isEmpty()) {
            return "redirect:/cart";
        }

        long totalCents = items.stream().mapToLong(CartItem::subtotalCents).sum();
        String token = UUID.randomUUID().toString();
        session.setAttribute("checkoutToken", token);

        model.addAttribute("items", items);
        model.addAttribute("totalCents", totalCents);
        model.addAttribute("checkoutToken", token);
        return "checkout";
    }

    @PostMapping("/checkout")
    public String checkout(@RequestParam String address,
                           @RequestParam String checkoutToken,
                           Principal principal,
                           HttpSession session,
                           RedirectAttributes redirect) {
        Object expected = session.getAttribute("checkoutToken");
        if (expected == null || !checkoutToken.equals(expected.toString())) {
            redirect.addFlashAttribute("error", "订单已提交，请勿重复操作");
            return "redirect:/cart";
        }
        session.removeAttribute("checkoutToken");

        if (address.strip().length() < 5 || address.strip().length() > 300) {
            redirect.addFlashAttribute("error", "收货信息需要 5 到 300 个字符");
            return "redirect:/checkout";
        }

        try {
            orders.checkout(currentUserId(principal), address);
            redirect.addFlashAttribute("message", "订单已创建，请完成付款");
            return "redirect:/orders";
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
            return "redirect:/cart";
        }
    }

    @GetMapping("/orders")
    public String orders(Principal principal, Model model) {
        model.addAttribute("orders", orders.list(currentUserId(principal)));
        return "orders";
    }

    private long currentUserId(Principal principal) {
        return users.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalStateException("登录用户不存在"))
                .id();
    }

    @PostMapping("/orders/pay")
    public String pay(@RequestParam long orderId, Principal principal,
                      RedirectAttributes redirect) {
        try {
            orders.pay(currentUserId(principal), orderId);
            redirect.addFlashAttribute("message", "付款成功（模拟）");
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/orders";
    }

    @PostMapping("/orders/cancel")
    public String cancel(@RequestParam long orderId, Principal principal,
                         RedirectAttributes redirect) {
        try {
            orders.cancel(currentUserId(principal), orderId);
            redirect.addFlashAttribute("message", "订单已取消，库存已恢复");
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/orders";
    }

    @PostMapping("/orders/complete")
    public String complete(@RequestParam long orderId, Principal principal,
                           RedirectAttributes redirect) {
        try {
            orders.complete(currentUserId(principal), orderId);
            redirect.addFlashAttribute("message", "已确认收货");
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/orders";
    }
}