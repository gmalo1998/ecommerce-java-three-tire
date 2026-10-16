
package com.gaurav.service.impl;

import com.gaurav.exception.ProductException;
import com.gaurav.model.Cart;
import com.gaurav.model.CartItem;
import com.gaurav.model.Product;
import com.gaurav.model.User;
import com.gaurav.repository.CartItemRepository;
import com.gaurav.repository.CartRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplementationTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CartServiceImplementation cartService;

    @Test
    void calculateDiscountPercentageShouldReturnCorrectValue() {
        assertEquals(
                20,
                CartServiceImplementation
                        .calculateDiscountPercentage(1000, 800)
        );
    }

    @Test
    void calculateDiscountPercentageShouldReturnZeroForZeroMrp() {
        assertEquals(
                0,
                CartServiceImplementation
                        .calculateDiscountPercentage(0, 800)
        );
    }

    @Test
    void findUserCartShouldCalculateTotals() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        Cart cart = mock(Cart.class);
        when(cartRepository.findByUserId(1L)).thenReturn(cart);

        Set<CartItem> items = new HashSet<>();

        CartItem item1 = mock(CartItem.class);
        when(item1.getMrpPrice()).thenReturn(1000);
        when(item1.getSellingPrice()).thenReturn(800);
        when(item1.getQuantity()).thenReturn(2);

        CartItem item2 = mock(CartItem.class);
        when(item2.getMrpPrice()).thenReturn(500);
        when(item2.getSellingPrice()).thenReturn(400);
        when(item2.getQuantity()).thenReturn(1);

        items.add(item1);
        items.add(item2);

        when(cart.getCartItems()).thenReturn(items);
        when(cart.getCouponPrice()).thenReturn(50);
        when(cartRepository.save(cart)).thenReturn(cart);

        Cart result = cartService.findUserCart(user);

        assertSame(cart, result);

        verify(cart).setTotalMrpPrice(1500);
        verify(cart).setTotalSellingPrice(1150);
        verify(cart).setTotalItem(3);
        verify(cart).setDiscount(20);

        verify(cartRepository).save(cart);
    }

    @Test
    void findUserCartShouldThrowWhenCartDoesNotExist() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        when(cartRepository.findByUserId(1L)).thenReturn(null);

        assertThrows(
                IllegalStateException.class,
                () -> cartService.findUserCart(user)
        );

        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void addCartItemShouldSaveNewItem() throws ProductException {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        Product product = mock(Product.class);
        when(product.getId()).thenReturn(10L);
        when(product.getMrpPrice()).thenReturn(1000);
        when(product.getSellingPrice()).thenReturn(800);

        Cart cart = mock(Cart.class);

        Set<CartItem> items = new HashSet<>();

        when(cartRepository.findByUserId(1L)).thenReturn(cart);
        when(cart.getCartItems()).thenReturn(items);
        when(cart.getCouponPrice()).thenReturn(0);
        when(cartRepository.save(cart)).thenReturn(cart);

        when(cartItemRepository.findByCartAndProductAndSize(
                cart, product, "M"
        )).thenReturn(null);

        when(cartItemRepository.save(any(CartItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CartItem result = cartService.addCartItem(
                user, product, "M", 2
        );

        assertNotNull(result);
        assertEquals(2, result.getQuantity());
        assertEquals(2000, result.getMrpPrice());
        assertEquals(1600, result.getSellingPrice());
        assertEquals("M", result.getSize());
        assertSame(cart, result.getCart());

        verify(cartItemRepository).save(any(CartItem.class));

        assertTrue(items.contains(result));
    }

    @Test
    void addCartItemShouldReturnExistingItem() throws ProductException {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        Product product = mock(Product.class);
        when(product.getId()).thenReturn(10L);

        Cart cart = mock(Cart.class);

        when(cart.getCartItems()).thenReturn(new HashSet<>());
        when(cart.getCouponPrice()).thenReturn(0);
        when(cartRepository.findByUserId(1L)).thenReturn(cart);
        when(cartRepository.save(cart)).thenReturn(cart);

        CartItem existingItem = mock(CartItem.class);

        when(cartItemRepository.findByCartAndProductAndSize(
                cart, product, "M"
        )).thenReturn(existingItem);

        CartItem result = cartService.addCartItem(
                user, product, "M", 2
        );

        assertSame(existingItem, result);

        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addCartItemShouldRejectZeroQuantity() {
        User user = mock(User.class);
        Product product = mock(Product.class);

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.addCartItem(user, product, "M", 0)
        );

        verifyNoInteractions(cartRepository, cartItemRepository);
    }
}

