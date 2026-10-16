
package com.gaurav.controller;

import com.gaurav.exception.ProductException;
import com.gaurav.model.Product;
import com.gaurav.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductById(
            @PathVariable Long productId) throws ProductException {

        log.debug("Fetching product by ID: {}", productId);

        Product product = productService.findProductById(productId);

        log.debug("Product lookup completed for ID: {}", productId);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProduct(
            @RequestParam(required = false) String query) {

        log.debug("Searching products. Query provided: {}", query != null && !query.isBlank());

        List<Product> products = productService.searchProduct(query);

        log.debug("Product search completed. Result count: {}", products.size());
        return ResponseEntity.ok(products);
    }

    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) Integer minDiscount,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String stock,
            @RequestParam(defaultValue = "0") Integer pageNumber) {

        log.debug("Fetching products. Page: {}, category filter provided: {}, brand filter provided: {}",
                pageNumber,
                category != null && !category.isBlank(),
                brand != null && !brand.isBlank());

        Page<Product> products = productService.getAllProduct(
                category,
                brand,
                color,
                size,
                minPrice,
                maxPrice,
                minDiscount,
                sort,
                stock,
                pageNumber
        );

        log.debug("Product listing completed. Page: {}, result count: {}",
                pageNumber, products.getNumberOfElements());

        return new ResponseEntity<>(products, HttpStatus.OK);
    }
}