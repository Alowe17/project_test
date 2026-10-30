package com.example.cicd.basket;

import java.math.BigDecimal;
import java.util.List;

public class CartSummary {
    private final List<CartItem> items;
    private final BigDecimal subtotal;
    private final BigDecimal tax;
    private final BigDecimal discount;
    private final BigDecimal total;

    public CartSummary(List<CartItem> items, BigDecimal subtotal, BigDecimal tax,
                       BigDecimal discount, BigDecimal total) {
        this.items = items;
        this.subtotal = subtotal;
        this.tax = tax;
        this.discount = discount;
        this.total = total;
    }

    public List<CartItem> getItems() { return items; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getTax() { return tax; }
    public BigDecimal getDiscount() { return discount; }
    public BigDecimal getTotal() { return total; }
}