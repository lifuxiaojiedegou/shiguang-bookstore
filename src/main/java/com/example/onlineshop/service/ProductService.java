package com.example.onlineshop.service;

import com.example.onlineshop.model.Product;
import com.example.onlineshop.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository products;

    public ProductService(ProductRepository products) {
        this.products = products;
    }

    public List<Product> search(String keyword,String category) {
        return products.search(keyword,category);
    }

    public List<String> listCategories() {
        return products.findCategories();
    }

    public List<Product> listAll() {
        return products.findAll();
    }

    public void save(Long id, String name, String category, String description,
                     int priceCents, int stock, boolean active) {
        String cleanName = name == null ? "" : name.strip();
        String cleanCategory = category == null ? "" : category.strip();
        String cleanDescription = description == null ? "" : description.strip();

        if (cleanName.isEmpty() || cleanName.length() > 80) {
            throw new IllegalArgumentException("商品名称需要 1 到 80 个字符");
        }
        if (cleanCategory.isEmpty() || cleanCategory.length() > 30) {
            throw new IllegalArgumentException("分类需要 1 到 30 个字符");
        }
        if (cleanDescription.isEmpty() || cleanDescription.length() > 300) {
            throw new IllegalArgumentException("描述需要 1 到 300 个字符");
        }
        if (priceCents < 1 || priceCents > 1_000_000) {
            throw new IllegalArgumentException("价格需要在 0.01 到 10000 元之间");
        }
        if (stock < 0 || stock > 100_000) {
            throw new IllegalArgumentException("库存需要在 0 到 100000 之间");
        }

        if (id == null || id == 0) {
            products.insert(cleanName, cleanCategory, cleanDescription, priceCents, stock, active);
        } else if (products.update(id, cleanName, cleanCategory, cleanDescription,
                priceCents, stock, active) == 0) {
            throw new IllegalArgumentException("商品不存在");
        }
    }

    public void setActive(long id, boolean active) {
        if (products.setActive(id, active) == 0) {
            throw new IllegalArgumentException("商品不存在");
        }
    }

    public Product get(long id) {
        return products.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("商品不存在"));
    }

}


