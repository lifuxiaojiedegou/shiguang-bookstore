package com.example.onlineshop.service;

// AI-assisted: Claude Code, 2026-10; reviewed and modified by 方喆.

import com.example.onlineshop.model.CartItem;
import com.example.onlineshop.model.Order;
import com.example.onlineshop.model.OrderItem;
import com.example.onlineshop.repository.CartRepository;
import com.example.onlineshop.repository.OrderRepository;
import com.example.onlineshop.repository.ProductRepository;
import com.example.onlineshop.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.example.onlineshop.model.ShopUser;
import com.example.onlineshop.repository.MailRepository;

@Service
public class OrderService {

    private final CartRepository carts;
    private final ProductRepository products;
    private final OrderRepository orders;
    private final UserRepository users;
    private final MailRepository mails;

    public OrderService(CartRepository carts, ProductRepository products,
                        OrderRepository orders, UserRepository users, MailRepository mails) {
        this.carts = carts;
        this.products = products;
        this.orders = orders;
        this.users = users;
        this.mails = mails;
    }

    @Transactional
    public long checkout(long userId, String address) {
        users.lock(userId);

        List<CartItem> items = carts.findByUser(userId);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("购物车为空");
        }

        long totalCents = 0;
        for (CartItem item : items) {
            if (!item.active()) {
                throw new IllegalArgumentException(item.name() + " 已下架");
            }
            if (!products.decreaseStock(item.productId(), item.quantity())) {
                throw new IllegalArgumentException(item.name() + " 库存不足");
            }
            totalCents += (long) item.priceCents() * item.quantity();
        }

        long orderId = orders.insertOrder(userId, address.strip(), totalCents);
        for (CartItem item : items) {
            orders.insertItem(orderId, item.productId(), item.name(),
                    item.priceCents(), item.quantity());
        }
        carts.deleteByUser(userId);
        return orderId;
    }

    public List<Order> list(long userId) {
        return orders.findByUser(userId);
    }

    @Transactional
    public void pay(long userId, long orderId) {
        if (orders.markPaid(orderId, userId) == 0) {
            throw new IllegalArgumentException("只能支付自己的待付款订单");
        }
    }

    @Transactional
    public void cancel(long userId, long orderId) {
        if (orders.markCancelled(orderId, userId) == 0) {
            throw new IllegalArgumentException("只能取消自己的待付款订单");
        }
        for (OrderItem item : orders.findItems(orderId)) {
            products.restoreStock(item.productId(), item.quantity());
        }
    }

    @Transactional
    public void complete(long userId, long orderId) {
        if (orders.markCompleted(orderId, userId) == 0) {
            throw new IllegalArgumentException("只有已发货订单才能确认收货");
        }
    }

    public List<Order> listAll() {
        return orders.findAll();
    }

    @Transactional
    public long ship(long orderId, String trackingNo) {
        String cleaned = trackingNo == null ? "" : trackingNo.strip();
        if (cleaned.length() < 3 || cleaned.length() > 80) {
            throw new IllegalArgumentException("物流单号需要 3 到 80 个字符");
        }
        if (orders.markShipped(orderId, cleaned) == 0) {
            throw new IllegalArgumentException("只有已付款订单才能发货");
        }

        Order order = orders.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));
        ShopUser customer = users.findById(order.userId())
                .orElseThrow(() -> new IllegalArgumentException("顾客不存在"));

        String subject = "拾光书店订单 #" + orderId + " 已发货";
        String body = shippingBody(order, customer, cleaned);
        return mails.insert(orderId, customer.email(), subject, body);
    }

    private String shippingBody(Order order, ShopUser customer, String trackingNo) {
        StringBuilder body = new StringBuilder();
        body.append(customer.name()).append("，您好：\n\n")
                .append("您的订单 #").append(order.id()).append(" 已发货。\n")
                .append("物流单号：").append(trackingNo).append("\n")
                .append("收货信息：").append(order.address()).append("\n\n")
                .append("订单明细：\n");
        for (OrderItem item : order.items()) {
            body.append("- ").append(item.name()).append(" × ").append(item.quantity())
                    .append("，¥").append(String.format("%.2f", item.subtotalCents() / 100.0))
                    .append("\n");
        }
        body.append("\n订单金额：¥").append(String.format("%.2f", order.totalCents() / 100.0))
                .append("\n\n感谢在拾光书店购书。\n");
        return body.toString();
    }

}
