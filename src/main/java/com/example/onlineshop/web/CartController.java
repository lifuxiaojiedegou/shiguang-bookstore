package com.example.onlineshop.web;

import com.example.onlineshop.model.CartItem;
import com.example.onlineshop.model.LogAction;
import com.example.onlineshop.model.Product;
import com.example.onlineshop.repository.UserRepository;
import com.example.onlineshop.service.CartService;
import com.example.onlineshop.service.LogService;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CartController {

    private final  CartService carts;
    private final UserRepository users;
    private final LogService logs;

    public CartController(CartService carts, UserRepository users, LogService logs) {
        this.carts = carts;
        this.users = users;
        this.logs = logs;
    }

    @GetMapping("/cart")
    public String cart(Principal principal, Model model) {
        long userId = currentUserId(principal);
        List<CartItem> items = carts.list(userId);
        long totalCents = items.stream().mapToLong(CartItem::subtotalCents).sum();
        model.addAttribute("items", items);
        model.addAttribute("totalCents", totalCents);
        return "cart";
    }

    @PostMapping("/cart/add")
    public String add(@RequestParam long productId,
                      @RequestParam(defaultValue = "1")  int quantity,
                      @RequestParam(defaultValue = "false") boolean fromDetail,
                      Principal principal,
                      HttpServletRequest request,
                      RedirectAttributes redirect) {
        try {
            Product product = carts.add(currentUserId(principal), productId, quantity);
            logs.record(principal, request, LogAction.ADD_CART,
                    product.id(), product.name(), "数量 " + quantity);
            redirect.addFlashAttribute("message", "已加入购物车");
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return fromDetail ? "redirect:/product/" + productId : "redirect:/";
    }

    @PostMapping("/cart/update")
    public String update(@RequestParam long productId,
                         @RequestParam int quantity,
                         Principal principal,
                         RedirectAttributes redirect) {
        try {
            carts.updateQuantity(currentUserId(principal), productId, quantity);
            redirect.addFlashAttribute("message", "数量已更新");
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/cart";
    }

@PostMapping("/cart/remove")
public String remove(@RequestParam long productId,
                     Principal principal) {
        carts.remove(currentUserId(principal), productId);
        return "redirect:/cart";
    }

    private long currentUserId(Principal principal) {
        return users.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("登录用户不存在"))
                .id();
    }
}
