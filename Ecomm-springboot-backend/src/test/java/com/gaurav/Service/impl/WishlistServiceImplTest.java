
package com.gaurav.service.impl;

import com.gaurav.exception.WishlistNotFoundException;
import com.gaurav.model.Product;
import com.gaurav.model.User;
import com.gaurav.model.Wishlist;
import com.gaurav.repository.WishlistRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceImplTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private User user;

    @Mock
    private Product product;

    @InjectMocks
    private WishlistServiceImpl wishlistService;

    private Wishlist wishlist;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        wishlist = new Wishlist();
        wishlist.setUser(user);
    }

    @Test
    void createWishlist_shouldSaveWishlistForUser() {
        when(user.getId()).thenReturn(USER_ID);

        when(wishlistRepository.save(any(Wishlist.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Wishlist result = wishlistService.createWishlist(user);

        assertNotNull(result);
        assertSame(user, result.getUser());

        verify(wishlistRepository).save(any(Wishlist.class));
    }

    @Test
    void createWishlist_shouldRejectNullUser() {
        assertThrows(
                IllegalArgumentException.class,
                () -> wishlistService.createWishlist(null)
        );

        verifyNoInteractions(wishlistRepository);
    }

    @Test
    void createWishlist_shouldRejectUserWithoutId() {
        when(user.getId()).thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> wishlistService.createWishlist(user)
        );

        verifyNoInteractions(wishlistRepository);
    }

    @Test
    void getWishlistByUserId_shouldReturnExistingWishlist() {
        when(user.getId()).thenReturn(USER_ID);
        when(wishlistRepository.findByUserId(USER_ID))
                .thenReturn(wishlist);

        Wishlist result = wishlistService.getWishlistByUserId(user);

        assertSame(wishlist, result);

        verify(wishlistRepository).findByUserId(USER_ID);
        verify(wishlistRepository, never()).save(any(Wishlist.class));
    }

    @Test
    void getWishlistByUserId_shouldCreateWishlistWhenMissing() {
        when(user.getId()).thenReturn(USER_ID);
        when(wishlistRepository.findByUserId(USER_ID))
                .thenReturn(null);

        when(wishlistRepository.save(any(Wishlist.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Wishlist result = wishlistService.getWishlistByUserId(user);

        assertNotNull(result);
        assertSame(user, result.getUser());

        verify(wishlistRepository).findByUserId(USER_ID);
        verify(wishlistRepository).save(any(Wishlist.class));
    }

    @Test
    void getWishlistByUserId_shouldRejectNullUser() {
        assertThrows(
                IllegalArgumentException.class,
                () -> wishlistService.getWishlistByUserId(null)
        );

        verifyNoInteractions(wishlistRepository);
    }

    @Test
    void addProductToWishlist_shouldAddProductWhenNotPresent()
            throws WishlistNotFoundException {

        when(user.getId()).thenReturn(USER_ID);
        when(wishlistRepository.findByUserId(USER_ID))
                .thenReturn(wishlist);
        when(wishlistRepository.save(wishlist))
                .thenReturn(wishlist);

        Set<Product> wishlistProducts = wishlist.getProducts();

assertNotNull(
        wishlistProducts,
        "Wishlist products collection must be initialized"
);

wishlistProducts.clear();

        Wishlist result =
                wishlistService.addProductToWishlist(user, product);

        assertSame(wishlist, result);
        assertTrue(result.getProducts().contains(product));

        verify(wishlistRepository).findByUserId(USER_ID);
        verify(wishlistRepository).save(wishlist);
    }

    @Test
    void addProductToWishlist_shouldRemoveProductWhenAlreadyPresent()
            throws WishlistNotFoundException {

        when(user.getId()).thenReturn(USER_ID);

        wishlist.getProducts().add(product);

        when(wishlistRepository.findByUserId(USER_ID))
                .thenReturn(wishlist);
        when(wishlistRepository.save(wishlist))
                .thenReturn(wishlist);

        Wishlist result =
                wishlistService.addProductToWishlist(user, product);

        assertFalse(result.getProducts().contains(product));

        verify(wishlistRepository).findByUserId(USER_ID);
        verify(wishlistRepository).save(wishlist);
    }

    @Test
    void addProductToWishlist_shouldRejectNullProduct() {
        assertThrows(
                IllegalArgumentException.class,
                () -> wishlistService.addProductToWishlist(user, null)
        );

        verifyNoInteractions(wishlistRepository);
    }

    @Test
    void addProductToWishlist_shouldRejectNullUser() {
        assertThrows(
                IllegalArgumentException.class,
                () -> wishlistService.addProductToWishlist(null, product)
        );

        verifyNoInteractions(wishlistRepository);
    }
}

