package com.example.onlineshop.web;

import com.example.onlineshop.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserService users;

    public AuthController(UserService users) {
        this.users = users;
    }

    @GetMapping("/register")
    public String registerForm(@RequestParam(defaultValue = "false") boolean success,
                               Model model) {
        if (success) {
            model.addAttribute("message", "注册成功，下一步实现登录。");
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirm,
            Model model) {
        model.addAttribute("name", name);
        model.addAttribute("email", email);

        if (name.strip().length() < 2 || name.strip().length() > 30) {
            return error(model, "姓名需要 2 到 30 个字符");
        }
        if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+") || email.length() > 254) {
            return error(model, "邮箱格式不正确");
        }
        if (password.length() < 8 || password.length() > 72) {
            return error(model, "密码需要 8 到 72 个字符");
        }
        if (!password.equals(confirm)) {
            return error(model, "两次输入的密码不一致");
        }

        try {
            users.register(name, email, password);
        } catch (IllegalArgumentException exception) {
            return error(model, exception.getMessage());
        }

        return "redirect:/register?success";
    }

    private String error(Model model, String message) {
        model.addAttribute("error", message);
        return "register";
    }

    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "邮箱或密码错误");
        }
        return "login";
    }
}
