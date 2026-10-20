package com.example.onlineshop.model;

public record OrderView(Order order, ShopUser customer, MailMessage latestMail){
}
