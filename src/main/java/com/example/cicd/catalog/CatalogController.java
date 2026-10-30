package com.example.cicd.catalog;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {
    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return ResponseEntity.ok(catalogService.search(name, category, minPrice, maxPrice));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> get(@PathVariable Long id) {
        return ResponseEntity.ok(catalogService.getRequired(id));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}