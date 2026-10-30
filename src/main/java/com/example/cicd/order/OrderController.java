package com.example.cicd.order;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, String> body) {
        Order order = orderService.checkout(
                userId, body.get("email"), body.get("shippingAddress"),
                body.get("paymentMethod"), body.get("paymentDetails"));
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
}