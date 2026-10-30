package com.example.cicd.catalog;

import com.example.cicd.common.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class CatalogService {
    private final ProductRepository productRepository;

    public CatalogService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<Product> search(String name, String category, BigDecimal minPrice, BigDecimal maxPrice) {
        String n = (name == null || name.isBlank()) ? null : name;
        String c = (category == null || category.isBlank()) ? null : category;
        return productRepository.search(n, c, minPrice, maxPrice);
    }

    @Transactional(readOnly = true)
    public Product getRequired(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new DomainException("Товар не найден: " + id));
    }

    @Transactional
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public void decreaseStock(Long productId, int quantity) {
        Product p = getRequired(productId);
        if (p.getStock() < quantity) {
            throw new DomainException("Недостаточно товара: " + p.getName());
        }
        p.setStock(p.getStock() - quantity);
        productRepository.save(p);
    }
}