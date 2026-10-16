
package com.gaurav.service.impl;

import com.gaurav.exception.CartItemException;
import com.gaurav.exception.UserException;
import com.gaurav.model.Cart;
import com.gaurav.model.CartItem;
import com.gaurav.model.Product;
import com.gaurav.model.User;
import com.gaurav.repository.CartItemRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartItemServiceImplementationTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CartItemServiceImplementation cartItemService;

    @Test
    void findCartItemByIdShouldReturnItem()
            throws CartItemException {

        Long itemId = 10L;
        CartItem item = mock(CartItem.class);

        when(cartItemRepository.findById(itemId))
                .thenReturn(Optional.of(item));

        CartItem result = cartItemService.findCartItemById(itemId);

        assertSame(item, result);
        verify(cartItemRepository).findById(itemId);
    }

    @Test
    void findCartItemByIdShouldThrowWhenItemDoesNotExist() {

        Long itemId = 99L;

        when(cartItemRepository.findById(itemId))
                .thenReturn(Optional.empty());

        assertThrows(
                CartItemException.class,
                () -> cartItemService.findCartItemById(itemId)
        );

        verify(cartItemRepository).findById(itemId);
    }

    @Test
    void updateCartItemShouldUpdateQuantityForOwner()
            throws Exception {

        Long userId = 1L;
        Long itemId = 10L;

        CartItem existingItem = mock(CartItem.class);
        CartItem requestItem = mock(CartItem.class);
        Cart cart = mock(Cart.class);
        User user = mock(User.class);
        Product product = mock(Product.class);

        when(cartItemRepository.findById(itemId))
                .thenReturn(Optional.of(existingItem));
        when(existingItem.getCart()).thenReturn(cart);
        when(cart.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(existingItem.getProduct()).thenReturn(product);
        when(requestItem.getQuantity()).thenReturn(2);

        when(cartItemRepository.save(existingItem))
                .thenReturn(existingItem);

        CartItem result = cartItemService.updateCartItem(
                userId, itemId, requestItem
        );

        assertSame(existingItem, result);

        verify(existingItem).setQuantity(2);
        verify(cartItemRepository).save(existingItem);
    }

    @Test
    void updateCartItemShouldRejectAnotherUsersItem() {

        Long userId = 1L;
        Long ownerId = 2L;
        Long itemId = 10L;

        CartItem existingItem = mock(CartItem.class);
        CartItem requestItem = mock(CartItem.class);
        Cart cart = mock(Cart.class);
        User user = mock(User.class);

        when(cartItemRepository.findById(itemId))
                .thenReturn(Optional.of(existingItem));
        when(existingItem.getCart()).thenReturn(cart);
        when(cart.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(ownerId);

        assertThrows(
                CartItemException.class,
                () -> cartItemService.updateCartItem(
                        userId, itemId, requestItem
                )
        );

        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void updateCartItemShouldRejectZeroQuantity() {

        Long userId = 1L;
        Long itemId = 10L;

        CartItem existingItem = mock(CartItem.class);
        CartItem requestItem = mock(CartItem.class);
        Cart cart = mock(Cart.class);
        User user = mock(User.class);

        when(cartItemRepository.findById(itemId))
                .thenReturn(Optional.of(existingItem));
        when(existingItem.getCart()).thenReturn(cart);
        when(cart.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(requestItem.getQuantity()).thenReturn(0);

        assertThrows(
                CartItemException.class,
                () -> cartItemService.updateCartItem(
                        userId, itemId, requestItem
                )
        );

        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void removeCartItemShouldDeleteItemForOwner()
            throws Exception {

        Long userId = 1L;
        Long itemId = 10L;

        CartItem item = mock(CartItem.class);
        Cart cart = mock(Cart.class);
        User user = mock(User.class);

        when(cartItemRepository.findById(itemId))
                .thenReturn(Optional.of(item));
        when(item.getCart()).thenReturn(cart);
        when(cart.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(item.getId()).thenReturn(itemId);

        cartItemService.removeCartItem(userId, itemId);

        verify(cartItemRepository).deleteById(itemId);
    }

    @Test
    void removeCartItemShouldRejectAnotherUsersItem() {

        Long userId = 1L;
        Long ownerId = 2L;
        Long itemId = 10L;

        CartItem item = mock(CartItem.class);
        Cart cart = mock(Cart.class);
        User user = mock(User.class);

        when(cartItemRepository.findById(itemId))
                .thenReturn(Optional.of(item));
        when(item.getCart()).thenReturn(cart);
        when(cart.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(ownerId);

        assertThrows(
                UserException.class,
                () -> cartItemService.removeCartItem(userId, itemId)
        );

        verify(cartItemRepository, never()).deleteById(anyLong());
    }
}

