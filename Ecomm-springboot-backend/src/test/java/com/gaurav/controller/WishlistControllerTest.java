
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistControllerTest {

    @Mock
    private WishlistService wishlistService;

    @Mock
    private ProductService productService;

    @Mock
    private UserService userService;

    @InjectMocks
    private WishlistController wishlistController;

    @Test
    void createWishlistShouldReturnCreatedWishlist()
            throws UserException {

        String jwt = "Bearer test-token";
        User user = mock(User.class);
        Wishlist wishlist = mock(Wishlist.class);

        when(userService.findUserProfileByJwt(jwt)).thenReturn(user);
        when(wishlistService.createWishlist(user)).thenReturn(wishlist);

        ResponseEntity<Wishlist> response =
                wishlistController.createWishlist(jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(wishlist, response.getBody());

        verify(userService).findUserProfileByJwt(jwt);
        verify(wishlistService).createWishlist(user);
    }

    @Test
    void getWishlistShouldReturnAuthenticatedUsersWishlist()
            throws UserException {

        String jwt = "Bearer test-token";
        User user = mock(User.class);
        Wishlist wishlist = mock(Wishlist.class);

        when(userService.findUserProfileByJwt(jwt)).thenReturn(user);
        when(wishlistService.getWishlistByUserId(user))
                .thenReturn(wishlist);

        ResponseEntity<Wishlist> response =
                wishlistController.getWishlistByUserId(jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(wishlist, response.getBody());

        verify(userService).findUserProfileByJwt(jwt);
        verify(wishlistService).getWishlistByUserId(user);
    }

    @Test
    void addProductToWishlistShouldReturnUpdatedWishlist()
            throws UserException, ProductException,
            WishlistNotFoundException {

        String jwt = "Bearer test-token";
        Long productId = 10L;

        User user = mock(User.class);
        Product product = mock(Product.class);
        Wishlist wishlist = mock(Wishlist.class);

        when(userService.findUserProfileByJwt(jwt)).thenReturn(user);
        when(productService.findProductById(productId)).thenReturn(product);
        when(wishlistService.addProductToWishlist(user, product))
                .thenReturn(wishlist);

        ResponseEntity<Wishlist> response =
                wishlistController.addProductToWishlist(productId, jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(wishlist, response.getBody());

        verify(userService).findUserProfileByJwt(jwt);
        verify(productService).findProductById(productId);
        verify(wishlistService).addProductToWishlist(user, product);
    }

    @Test
    void addProductToWishlistShouldPropagateProductException()
            throws UserException, ProductException,
            WishlistNotFoundException {

        String jwt = "Bearer test-token";
        Long productId = 10L;
        User user = mock(User.class);

        when(userService.findUserProfileByJwt(jwt)).thenReturn(user);
        when(productService.findProductById(productId))
                .thenThrow(new ProductException("Product not found"));

        org.junit.jupiter.api.Assertions.assertThrows(
                ProductException.class,
                () -> wishlistController.addProductToWishlist(productId, jwt)
        );

        verify(wishlistService, never()).addProductToWishlist(
                any(User.class), any(Product.class));
    }
}

