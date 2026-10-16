
package com.gaurav.service.impl;

import com.gaurav.exception.WishlistNotFoundException;
import com.gaurav.model.Product;
import com.gaurav.model.User;
import com.gaurav.model.Wishlist;
import com.gaurav.repository.WishlistRepository;
import com.gaurav.service.WishlistService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;

    @Override
    @Transactional
    public Wishlist createWishlist(User user) {
        if (user == null || user.getId() == null) {
            log.warn("Wishlist creation rejected: user or user ID is missing");
            throw new IllegalArgumentException("Valid user is required");
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);

        Wishlist savedWishlist = wishlistRepository.save(wishlist);

        log.info("Wishlist created successfully for user ID: {}", user.getId());

        return savedWishlist;
    }

    @Override
    @Transactional
    public Wishlist getWishlistByUserId(User user) {
        if (user == null || user.getId() == null) {
            log.warn("Wishlist lookup rejected: user or user ID is missing");
            throw new IllegalArgumentException("Valid user is required");
        }

        Long userId = user.getId();

        log.debug("Fetching wishlist for user ID: {}", userId);

        Wishlist wishlist = wishlistRepository.findByUserId(userId);

        if (wishlist == null) {
            log.info("No wishlist found for user ID: {}. Creating one", userId);
            return createWishlist(user);
        }

        log.debug("Wishlist retrieved successfully for user ID: {}", userId);

        return wishlist;
    }

    @Override
    @Transactional
    public Wishlist addProductToWishlist(User user, Product product)
            throws WishlistNotFoundException {

        if (user == null || user.getId() == null) {
            log.warn("Wishlist update rejected: user or user ID is missing");
            throw new IllegalArgumentException("Valid user is required");
        }

        if (product == null) {
            log.warn("Wishlist update rejected: product is missing");
            throw new IllegalArgumentException("Product must not be null");
        }

        Wishlist wishlist = getWishlistByUserId(user);

        if (wishlist == null) {
            log.error("Unable to retrieve or create wishlist for user ID: {}",
                    user.getId());
            throw new WishlistNotFoundException("Wishlist not found");
        }

        if (wishlist.getProducts() == null) {
            log.error("Wishlist product collection is not initialized for user ID: {}",
                    user.getId());
            throw new IllegalStateException(
                    "Wishlist product collection must be initialized");
        }

        if (wishlist.getProducts().contains(product)) {
            wishlist.getProducts().remove(product);
            log.info("Product removed from wishlist for user ID: {}",
                    user.getId());
        } else {
            wishlist.getProducts().add(product);
            log.info("Product added to wishlist for user ID: {}",
                    user.getId());
        }

        return wishlistRepository.save(wishlist);
    }
}

