package com.example.onlineshop.model;

public record MailMessage(
        long id,
        long orderId,
        String recipient,
        String subject,
        String body,
        String status,
        String error) {

    public String statusLabel() {
        return switch (status) {
            case "PENDING" -> "待发送";
            case "SENDING" -> "发送中";
            case "SENT" -> "已发送";
            case "SAVED" -> "已保存演示邮件";
            case "FAILED" -> "发送失败";
            default -> status;
        };
    }
}
