package com.example.onlineshop.web;

import com.example.onlineshop.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ShopController {

    private final ProductService products;

    public ShopController(ProductService products) {
        this.products = products;
    }

    @GetMapping("/")
    public String home(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "") String category,
            Model model) {
        model.addAttribute("products", products.search(q, category));
        model.addAttribute("categories", products.listCategories());
        model.addAttribute("q", q);
        model.addAttribute("category", category);
        return "index";
    }
}
