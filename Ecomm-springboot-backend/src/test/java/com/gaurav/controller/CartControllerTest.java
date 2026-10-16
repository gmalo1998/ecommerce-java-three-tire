
package com.gaurav.controller;

import com.gaurav.exception.CartItemException;
import com.gaurav.exception.ProductException;
import com.gaurav.exception.UserException;
import com.gaurav.model.Cart;
import com.gaurav.model.CartItem;
import com.gaurav.model.Product;
import com.gaurav.model.User;
import com.gaurav.request.AddItemRequest;
import com.gaurav.response.ApiResponse;
import com.gaurav.service.CartItemService;
import com.gaurav.service.CartService;
import com.gaurav.service.ProductService;
import com.gaurav.service.UserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    private static final String JWT = "test-jwt-token";
    private static final Long USER_ID = 1L;
    private static final Long PRODUCT_ID = 10L;
    private static final Long CART_ITEM_ID = 100L;

    @Mock
    private CartService cartService;

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @Mock
    private CartItemService cartItemService;

    @InjectMocks
    private CartController cartController;

    @Test
    void findUserCartHandler_shouldReturnCart() throws UserException {
        User user = mock(User.class);
        Cart cart = mock(Cart.class);

        when(user.getId()).thenReturn(USER_ID);
        when(userService.findUserProfileByJwt(JWT)).thenReturn(user);
        when(cartService.findUserCart(user)).thenReturn(cart);

        ResponseEntity<Cart> response =
                cartController.findUserCartHandler(JWT);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(cart, response.getBody());

        verify(userService).findUserProfileByJwt(JWT);
        verify(cartService).findUserCart(user);
    }

    @Test
    void addItemToCart_shouldReturnAcceptedResponse()
            throws UserException, ProductException {

        User user = mock(User.class);
        Product product = mock(Product.class);
        AddItemRequest request = mock(AddItemRequest.class);
        CartItem cartItem = mock(CartItem.class);

        when(user.getId()).thenReturn(USER_ID);
        when(request.getProductId()).thenReturn(PRODUCT_ID);
        when(request.getSize()).thenReturn("M");
        when(request.getQuantity()).thenReturn(2);

        when(userService.findUserProfileByJwt(JWT)).thenReturn(user);
        when(productService.findProductById(PRODUCT_ID)).thenReturn(product);
        when(cartService.addCartItem(user, product, "M", 2))
                .thenReturn(cartItem);

        ResponseEntity<CartItem> response =
                cartController.addItemToCart(request, JWT);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertSame(cartItem, response.getBody());

        verify(cartService).addCartItem(user, product, "M", 2);
    }

    @Test
    void deleteCartItemHandler_shouldRemoveItem()
            throws UserException, CartItemException {

        User user = mock(User.class);
        when(user.getId()).thenReturn(USER_ID);
        when(userService.findUserProfileByJwt(JWT)).thenReturn(user);

        ResponseEntity<ApiResponse> response =
                cartController.deleteCartItemHandler(CART_ITEM_ID, JWT);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Item Remove From Cart", response.getBody().getMessage());
        assertTrue(response.getBody().isStatus());

        verify(cartItemService).removeCartItem(USER_ID, CART_ITEM_ID);
    }

    @Test
    void updateCartItemHandler_shouldUpdateItem()
            throws UserException, CartItemException {

        User user = mock(User.class);
        CartItem requestItem = mock(CartItem.class);
        CartItem updatedItem = mock(CartItem.class);

        when(user.getId()).thenReturn(USER_ID);
        when(userService.findUserProfileByJwt(JWT)).thenReturn(user);
        when(requestItem.getQuantity()).thenReturn(3);

        when(cartItemService.updateCartItem(
                USER_ID, CART_ITEM_ID, requestItem
        )).thenReturn(updatedItem);

        ResponseEntity<CartItem> response =
                cartController.updateCartItemHandler(CART_ITEM_ID, requestItem, JWT);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertSame(updatedItem, response.getBody());

        verify(cartItemService).updateCartItem(
                USER_ID, CART_ITEM_ID, requestItem
        );
    }

    @Test
    void updateCartItemHandler_shouldRejectNonPositiveQuantity()
            throws UserException, CartItemException {

        User user = mock(User.class);
        CartItem requestItem = mock(CartItem.class);

        when(userService.findUserProfileByJwt(JWT)).thenReturn(user);
        when(requestItem.getQuantity()).thenReturn(0);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> cartController.updateCartItemHandler(
                        CART_ITEM_ID, requestItem, JWT
                )
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verify(cartItemService, never()).updateCartItem(
                anyLong(), anyLong(), any(CartItem.class)
        );
    }
}