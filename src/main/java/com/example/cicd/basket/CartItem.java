package com.example.cicd.basket;

import com.example.cicd.catalog.Product;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String userId;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    @Column(nullable = false)
    private int quantity;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    public CartItem() {}
    public CartItem(String userId, Product product, int quantity) {
        this.userId = userId;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.getPrice();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}