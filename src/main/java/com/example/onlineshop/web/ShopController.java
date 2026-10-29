package com.example.onlineshop.web;

import com.example.onlineshop.model.LogAction;
import com.example.onlineshop.model.Product;
import com.example.onlineshop.service.LogService;
import com.example.onlineshop.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class ShopController {

    private final ProductService products;
    private final LogService logs;

    public ShopController(ProductService products, LogService logs) {
        this.products = products;
        this.logs = logs;
    }

    @GetMapping("/")
    public String home(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "") String category,
            Principal principal,
            HttpServletRequest request,
            Model model) {
        model.addAttribute("products", products.search(q, category));
        model.addAttribute("categories", products.listCategories());
        model.addAttribute("q", q);
        model.addAttribute("category", category);
        if (!q.isBlank()) {
            logs.record(principal, request, LogAction.SEARCH, null, null, q.strip());
        }
        return "index";
    }

    @GetMapping("/product/{id}")
    public String product(@PathVariable long id, Principal principal,
                          HttpServletRequest request, Model model) {
        Product product;
        try {
            product = products.get(id);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (!product.active()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        logs.record(principal, request, LogAction.VIEW, product.id(), product.name(), null);
        model.addAttribute("product", product);
        return "product";
    }
}
