
package com.gaurav.service.impl;

import com.gaurav.exception.CartItemException;
import com.gaurav.exception.UserException;
import com.gaurav.model.CartItem;
import com.gaurav.model.User;
import com.gaurav.repository.CartItemRepository;
import com.gaurav.service.CartItemService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartItemServiceImplementation implements CartItemService {

    private final CartItemRepository cartItemRepository;

    @Override
    public CartItem updateCartItem(
            Long userId,
            Long id,
            CartItem cartItem
    ) throws CartItemException, UserException {

        log.info("Request received to update cart item ID {}", id);

        CartItem item = findCartItemById(id);
        User cartItemUser = item.getCart().getUser();

        // Verify that the cart item belongs to the requesting user
        if (!cartItemUser.getId().equals(userId)) {
            log.warn("Cart item update rejected due to ownership mismatch");
            throw new CartItemException(
                    "You can't update another user's cart item"
            );
        }

        // Quantity is primitive int, so it cannot be null
        if (cartItem.getQuantity() <= 0) {
            throw new CartItemException(
                    "Cart item quantity must be greater than zero"
            );
        }

        // Update quantity
        item.setQuantity(cartItem.getQuantity());

        // Recalculate prices based on the updated quantity
        item.setMrpPrice(
                item.getQuantity() * item.getProduct().getMrpPrice()
        );

        item.setSellingPrice(
                item.getQuantity() * item.getProduct().getSellingPrice()
        );

        CartItem updatedItem = cartItemRepository.save(item);

        log.info("Cart item ID {} updated successfully", id);

        return updatedItem;
    }

    @Override
    public void removeCartItem(Long userId, Long cartItemId)
            throws CartItemException, UserException {

        log.info("Request received to remove cart item ID {}", cartItemId);

        CartItem cartItem = findCartItemById(cartItemId);
        User cartItemUser = cartItem.getCart().getUser();

        // Verify ownership before deleting
        if (!cartItemUser.getId().equals(userId)) {
            log.warn("Cart item removal rejected due to ownership mismatch");
            throw new UserException(
                    "You can't remove another user's cart item"
            );
        }

        cartItemRepository.deleteById(cartItem.getId());

        log.info("Cart item ID {} removed successfully", cartItemId);
    }

    @Override
    public CartItem findCartItemById(Long cartItemId)
            throws CartItemException {

        Optional<CartItem> optionalCartItem =
                cartItemRepository.findById(cartItemId);

        if (optionalCartItem.isPresent()) {
            return optionalCartItem.get();
        }

        log.warn("Cart item ID {} was not found", cartItemId);

        throw new CartItemException(
                "Cart item not found with ID: " + cartItemId
        );
    }
}

