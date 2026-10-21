package com.example.onlineshop.web;

// AI-assisted: Claude Code, 2026-10; reviewed and modified by 方喆.

import com.example.onlineshop.model.Product;
import com.example.onlineshop.service.ProductService;
import java.math.BigDecimal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private final ProductService products;

    public AdminProductController(ProductService products) {
        this.products = products;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", products.listAll());
        return "admin/products";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("product", null);
        model.addAttribute("isNew", true);
        return "admin/product-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable long id, Model model) {
        model.addAttribute("product", products.get(id));
        model.addAttribute("isNew", false);
        return "admin/product-form";
    }

    @PostMapping("/save")
    public String save(@RequestParam(defaultValue = "0") long id,
                       @RequestParam String name,
                       @RequestParam String category,
                       @RequestParam String description,
                       @RequestParam String price,
                       @RequestParam int stock,
                       @RequestParam(defaultValue = "false") boolean active,
                       RedirectAttributes redirect) {
        try {
            int priceCents = parsePriceCents(price);
            products.save(id, name, category, description, priceCents, stock, active);
            redirect.addFlashAttribute("message", id == 0 ? "商品已添加" : "商品已更新");
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/toggle")
    public String toggle(@RequestParam long id,
                         @RequestParam boolean active,
                         RedirectAttributes redirect) {
        try {
            products.setActive(id, active);
            redirect.addFlashAttribute("message", active ? "商品已上架" : "商品已下架");
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam long id, RedirectAttributes redirect) {
        try {
            products.delete(id);
            redirect.addFlashAttribute("message", "商品已删除");
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/products";
    }

    private int parsePriceCents(String priceText) {
        try {
            return new BigDecimal(priceText.strip())
                    .movePointRight(2)
                    .intValueExact();
        } catch (NumberFormatException | ArithmeticException exception) {
            throw new IllegalArgumentException("价格格式不正确");
        }
    }
}