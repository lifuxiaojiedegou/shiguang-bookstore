package com.example.onlineshop.model;

public enum LogAction {
    VIEW("浏览"),
    SEARCH("搜索"),
    ADD_CART("加购"),
    ORDER("下单");

    private final String label;

    LogAction(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
