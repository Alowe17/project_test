package com.example.cicd.basket;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/basket")
public class BasketController {
    private final BasketService basketService;

    public BasketController(BasketService basketService) {
        this.basketService = basketService;
    }

    @PostMapping("/add")
    public ResponseEntity<CartItem> add(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(body.get("productId").toString());
        int quantity = body.containsKey("quantity")
                ? Integer.parseInt(body.get("quantity").toString()) : 1;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(basketService.addItem(userId, productId, quantity));
    }

    @GetMapping
    public ResponseEntity<CartSummary> get(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(basketService.getSummary(userId));
    }
}