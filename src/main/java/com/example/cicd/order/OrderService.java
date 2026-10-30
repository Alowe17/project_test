package com.example.cicd.order;

import com.example.cicd.basket.BasketService;
import com.example.cicd.basket.CartItem;
import com.example.cicd.basket.CartSummary;
import com.example.cicd.catalog.CatalogService;
import com.example.cicd.common.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class OrderService {
    private static final Pattern CARD = Pattern.compile("^\\d{13,19}$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final OrderRepository orderRepository;
    private final BasketService basketService;
    private final CatalogService catalogService;

    public OrderService(OrderRepository orderRepository, BasketService basketService, CatalogService catalogService) {
        this.orderRepository = orderRepository;
        this.basketService = basketService;
        this.catalogService = catalogService;
    }

    @Transactional
    public Order checkout(String userId, String email, String shippingAddress,
                          String paymentMethod, String paymentDetails) {
        validatePayment(paymentMethod, paymentDetails);
        if (shippingAddress == null || shippingAddress.isBlank()) {
            throw new DomainException("Адрес доставки обязателен");
        }
        if (email == null || !EMAIL.matcher(email).matches()) {
            throw new DomainException("Неверный email");
        }
        CartSummary cart = basketService.getSummary(userId);
        List<CartItem> items = cart.getItems();
        if (items == null || items.isEmpty()) {
            throw new DomainException("Корзина пуста — невозможно оформить заказ");
        }
        for (CartItem item : items) {
            catalogService.decreaseStock(item.getProduct().getId(), item.getQuantity());
        }
        Order order = new Order();
        order.setUserId(userId);
        order.setEmail(email);
        order.setShippingAddress(shippingAddress);
        order.setPaymentMethod(paymentMethod);
        order.setPaymentDetails(mask(paymentMethod, paymentDetails));
        order.setTotalAmount(cart.getTotal());
        order.setTaxAmount(cart.getTax());
        order.setDiscountAmount(cart.getDiscount());
        order.setStatus(Order.Status.CONFIRMED);
        for (CartItem ci : items) {
            order.getItems().add(new OrderItem(
                    ci.getProduct().getId(), ci.getProduct().getName(),
                    ci.getQuantity(), ci.getUnitPrice()));
        }
        Order saved = orderRepository.save(order);
        basketService.clear(userId);
        return saved;
    }

    private void validatePayment(String method, String details) {
        if ("CREDIT_CARD".equals(method)) {
            String digits = details == null ? "" : details.replaceAll("\\s+", "");
            if (!CARD.matcher(digits).matches()) {
                throw new DomainException("Неверный номер кредитной карты");
            }
        } else if ("PAYPAL".equals(method)) {
            if (details == null || !EMAIL.matcher(details).matches()) {
                throw new DomainException("Неверный email PayPal");
            }
        } else {
            throw new DomainException("Неподдерживаемый способ оплаты: " + method);
        }
    }

    private String mask(String method, String details) {
        if ("CREDIT_CARD".equals(method)) {
            String d = details.replaceAll("\\s+", "");
            return d.length() >= 4 ? "****" + d.substring(d.length() - 4) : "****";
        }
        return details;
    }
}