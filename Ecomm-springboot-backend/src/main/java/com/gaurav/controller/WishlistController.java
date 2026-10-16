
package com.gaurav.controller;

import com.gaurav.exception.ProductException;
import com.gaurav.exception.UserException;
import com.gaurav.exception.WishlistNotFoundException;

import com.gaurav.model.Product;
import com.gaurav.model.User;
import com.gaurav.model.Wishlist;

import com.gaurav.service.ProductService;
import com.gaurav.service.UserService;
import com.gaurav.service.WishlistService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Slf4j
public class WishlistController {

    private final WishlistService wishlistService;
    private final ProductService productService;
    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<Wishlist> createWishlist(
            @RequestHeader("Authorization") String jwt)
            throws UserException {

        log.info("Received request to create wishlist");

        User user = userService.findUserProfileByJwt(jwt);
        Wishlist wishlist = wishlistService.createWishlist(user);

        log.info("Wishlist created successfully for authenticated user");

        return ResponseEntity.ok(wishlist);
    }

    @GetMapping
    public ResponseEntity<Wishlist> getWishlistByUserId(
            @RequestHeader("Authorization") String jwt)
            throws UserException {

        log.info("Received request to fetch authenticated user's wishlist");

        User user = userService.findUserProfileByJwt(jwt);
        Wishlist wishlist = wishlistService.getWishlistByUserId(user);

        log.info("Wishlist fetched successfully");

        return ResponseEntity.ok(wishlist);
    }

    @PostMapping("/add-product/{productId}")
    public ResponseEntity<Wishlist> addProductToWishlist(
            @PathVariable Long productId,
            @RequestHeader("Authorization") String jwt)
            throws WishlistNotFoundException, ProductException, UserException {

        log.info("Received request to add product ID {} to wishlist", productId);

        User user = userService.findUserProfileByJwt(jwt);
        Product product = productService.findProductById(productId);

        Wishlist updatedWishlist =
                wishlistService.addProductToWishlist(user, product);

        log.info("Product ID {} added to wishlist successfully", productId);

        return ResponseEntity.ok(updatedWishlist);
    }
}

