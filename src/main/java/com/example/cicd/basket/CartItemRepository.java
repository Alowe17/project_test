package com.example.cicd.basket;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserId(String userId);
    Optional<CartItem> findByUserIdAndProductId(String userId, Long productId);
    void deleteByUserId(String userId);
}