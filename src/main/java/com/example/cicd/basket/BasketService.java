package com.example.cicd.basket;

import com.example.cicd.catalog.CatalogService;
import com.example.cicd.catalog.Product;
import com.example.cicd.common.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class BasketService {
    private static final BigDecimal TAX_RATE = new BigDecimal("0.20");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");

    private final CartItemRepository cartItemRepository;
    private final CatalogService catalogService;

    public BasketService(CartItemRepository cartItemRepository, CatalogService catalogService) {
        this.cartItemRepository = cartItemRepository;
        this.catalogService = catalogService;
    }

    @Transactional
    public CartItem addItem(String userId, Long productId, int quantity) {
        if (quantity < 1) throw new DomainException("Количество должно быть >= 1");
        Product product = catalogService.getRequired(productId);
        if (product.getStock() < quantity) {
            throw new DomainException("Недостаточно товара: " + product.getName());
        }
        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, productId).orElse(null);
        if (item != null) {
            int newQty = item.getQuantity() + quantity;
            if (product.getStock() < newQty) {
                throw new DomainException("Недостаточно товара: " + product.getName());
            }
            item.setQuantity(newQty);
        } else {
            item = new CartItem(userId, product, quantity);
        }
        return cartItemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public CartSummary getSummary(String userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        BigDecimal subtotal = items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = BigDecimal.ZERO;
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) >= 0) {
            discount = subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal taxable = subtotal.subtract(discount);
        BigDecimal tax = taxable.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        return new CartSummary(items, subtotal, tax, discount, taxable.add(tax));
    }

    @Transactional
    public void clear(String userId) {
        cartItemRepository.deleteByUserId(userId);
    }
}