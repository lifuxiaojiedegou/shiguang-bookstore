package com.example.onlineshop.service;

// AI-assisted: Claude Code, 2026-10; reviewed and modified by 方喆.

import com.example.onlineshop.model.CartItem;
import com.example.onlineshop.model.Product;
import com.example.onlineshop.repository.CartRepository;
import com.example.onlineshop.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    private final CartRepository carts;
    private final ProductRepository products;

    public CartService(CartRepository carts, ProductRepository products) {
        this.carts = carts;
        this.products = products;
    }

    public List<CartItem> list(long userId) {
        return carts.findByUser(userId);
    }

    public Product add(long userId, long productId, int quantity) {
        if (quantity < 1 || quantity > 99) {
            throw new IllegalArgumentException("数量必须在 1 到 99 之间");
        }
        Product product = products.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("商品不存在"));
        if (!product.active()) {
            throw new IllegalArgumentException("商品已下架");
        }

        int current = carts.findQuantity(userId, productId).orElse(0);
        int target = Math.min(current + quantity, 99);
        if (target > product.stock()) {
            throw new IllegalArgumentException("库存不足");
        }
        carts.save(userId, productId, target);
        return product;
    }

    public void updateQuantity(long userId, long productId, int quantity) {
        if (quantity < 1 || quantity > 99) {
            throw new IllegalArgumentException("数量必须在 1 到 99 之间");
        }
        Product product = products.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("商品不存在"));
        if (!product.active()) {
            throw new IllegalArgumentException("商品已下架");
        }
        if (quantity > product.stock()) {
            throw new IllegalArgumentException("库存不足");
        }
        carts.save(userId, productId, quantity);
    }

    public void remove(long userId, long productId) {
        carts.delete(userId, productId);
    }
}
