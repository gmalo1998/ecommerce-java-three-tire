
package com.gaurav.service.impl;

import com.gaurav.exception.ProductException;
import com.gaurav.model.Cart;
import com.gaurav.model.CartItem;
import com.gaurav.model.Product;
import com.gaurav.model.User;
import com.gaurav.repository.CartItemRepository;
import com.gaurav.repository.CartRepository;
import com.gaurav.service.CartService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImplementation implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    public Cart findUserCart(User user) {

        log.info("Fetching cart for user ID {}", user.getId());

        Cart cart = cartRepository.findByUserId(user.getId());

        if (cart == null) {
            log.warn("Cart not found for user ID {}", user.getId());
            throw new IllegalStateException("Cart not found for user");
        }

        int totalMrpPrice = 0;
        int totalSellingPrice = 0;
        int totalItems = 0;

        for (CartItem cartItem : cart.getCartItems()) {
            totalMrpPrice += cartItem.getMrpPrice();
            totalSellingPrice += cartItem.getSellingPrice();
            totalItems += cartItem.getQuantity();
        }

        cart.setTotalMrpPrice(totalMrpPrice);
        cart.setTotalItem(totalItems);
        cart.setTotalSellingPrice(
                totalSellingPrice - cart.getCouponPrice()
        );
        cart.setDiscount(
                calculateDiscountPercentage(
                        totalMrpPrice, totalSellingPrice
                )
        );

        Cart savedCart = cartRepository.save(cart);

        log.info("Cart totals calculated for user ID {}", user.getId());

        return savedCart;
    }

    public static int calculateDiscountPercentage(
            double mrpPrice,
            double sellingPrice) {

        if (mrpPrice <= 0) {
            return 0;
        }

        double discount = mrpPrice - sellingPrice;
        double discountPercentage = (discount / mrpPrice) * 100;

        return (int) discountPercentage;
    }

    @Override
    public CartItem addCartItem(
            User user,
            Product product,
            String size,
            int quantity
    ) throws ProductException {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Cart item quantity must be greater than zero"
            );
        }

        log.info("Adding product ID {} to cart for user ID {}",
                product.getId(), user.getId());

        Cart cart = findUserCart(user);

        CartItem existingItem =
                cartItemRepository.findByCartAndProductAndSize(
                        cart, product, size
                );

        if (existingItem != null) {
            log.info("Product already exists in cart for requested size");
            return existingItem;
        }

        CartItem cartItem = new CartItem();

        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        cartItem.setUserId(user.getId());
        cartItem.setSellingPrice(
                quantity * product.getSellingPrice()
        );
        cartItem.setMrpPrice(
                quantity * product.getMrpPrice()
        );
        cartItem.setSize(size);

        cart.getCartItems().add(cartItem);
        cartItem.setCart(cart);

        CartItem savedItem = cartItemRepository.save(cartItem);

        log.info("Product added to cart successfully for user ID {}",
                user.getId());

        return savedItem;
    }
}

