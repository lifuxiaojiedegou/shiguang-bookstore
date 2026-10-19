package com.example.onlineshop.service;

import com.example.onlineshop.repository.UserRepository;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(String name, String email, String password) {
        String normalizedEmail = email.strip().toLowerCase(Locale.ROOT);
        if (users.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("该邮箱已注册");
        }
        users.insert(normalizedEmail, name.strip(),
                passwordEncoder.encode(password),"CUSTOMER");
    }
}
