
package com.gaurav.controller;

import com.gaurav.exception.CategoryNotFoundException;
import com.gaurav.exception.ProductException;
import com.gaurav.exception.SellerException;
import com.gaurav.exception.UserException;
import com.gaurav.model.Product;
import com.gaurav.model.Seller;
import com.gaurav.request.CreateProductRequest;
import com.gaurav.service.ProductService;
import com.gaurav.service.SellerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/sellers/product")
@RequiredArgsConstructor
public class SellerProductController {

    private final ProductService productService;
    private final SellerService sellerService;

    @GetMapping
    public ResponseEntity<List<Product>> getProductBySellerId(
            @RequestHeader("Authorization") String jwt)
            throws ProductException, SellerException {

        Seller seller = sellerService.getSellerProfile(jwt);

        log.debug("Fetching products for seller ID: {}", seller.getId());

        List<Product> products =
                productService.getProductBySellerId(seller.getId());

        log.debug("Product retrieval completed for seller ID: {}. Result count: {}",
                seller.getId(), products.size());

        return ResponseEntity.ok(products);
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(
            @RequestBody CreateProductRequest request,
            @RequestHeader("Authorization") String jwt)
            throws UserException, ProductException,
            CategoryNotFoundException, SellerException {

        Seller seller = sellerService.getSellerProfile(jwt);

        log.info("Product creation requested by seller ID: {}", seller.getId());

        Product product = productService.createProduct(request, seller);

        log.info("Product created successfully for seller ID: {}", seller.getId());

        return new ResponseEntity<>(product, HttpStatus.CREATED);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long productId,
            @RequestHeader("Authorization") String jwt)
            throws ProductException, SellerException {

        Seller seller = sellerService.getSellerProfile(jwt);

        log.info("Product deletion requested. Product ID: {}, seller ID: {}",
                productId, seller.getId());

        // IMPORTANT: productService must verify that this product
        // belongs to the authenticated seller before deleting it.
        productService.deleteProduct(productId);

        log.info("Product deletion completed. Product ID: {}", productId);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long productId,
            @RequestBody Product product,
            @RequestHeader("Authorization") String jwt)
            throws ProductException, SellerException {

        Seller seller = sellerService.getSellerProfile(jwt);

        log.info("Product update requested. Product ID: {}, seller ID: {}",
                productId, seller.getId());

        // IMPORTANT: productService must verify ownership and restrict
        // which product fields the seller is allowed to update.
        Product updatedProduct = productService.updateProduct(productId, product);

        log.info("Product update completed. Product ID: {}", productId);

        return ResponseEntity.ok(updatedProduct);
    }
}