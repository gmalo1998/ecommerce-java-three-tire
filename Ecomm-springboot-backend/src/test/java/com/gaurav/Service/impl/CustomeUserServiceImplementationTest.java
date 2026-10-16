
package com.gaurav.service.impl;

import com.gaurav.domain.USER_ROLE;
import com.gaurav.model.Seller;
import com.gaurav.model.User;
import com.gaurav.repository.SellerRepository;
import com.gaurav.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomeUserServiceImplementationTest {

    private static final String CUSTOMER_EMAIL = "customer@example.com";
    private static final String SELLER_EMAIL = "seller@example.com";
    private static final String ENCODED_PASSWORD = "$2a$encodedPassword";

    @Mock
    private UserRepository userRepository;

    @Mock
    private SellerRepository sellerRepository;

    @InjectMocks
    private CustomeUserServiceImplementation userDetailsService;

    private User customer;
    private Seller seller;

    @BeforeEach
    void setUp() {
        customer = mock(User.class);
        seller = mock(Seller.class);
    }

    @Test
    void loadUserByUsername_whenCustomerExists_returnsCustomerDetails() {
        when(userRepository.findByEmail(CUSTOMER_EMAIL))
                .thenReturn(customer);
        when(customer.getEmail()).thenReturn(CUSTOMER_EMAIL);
        when(customer.getPassword()).thenReturn(ENCODED_PASSWORD);
        when(customer.getRole()).thenReturn(USER_ROLE.ROLE_CUSTOMER);

        UserDetails result =
                userDetailsService.loadUserByUsername(CUSTOMER_EMAIL);

        assertEquals(CUSTOMER_EMAIL, result.getUsername());
        assertEquals(ENCODED_PASSWORD, result.getPassword());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER")));

        verify(userRepository).findByEmail(CUSTOMER_EMAIL);
        verifyNoInteractions(sellerRepository);
    }

    @Test
    void loadUserByUsername_whenSellerExists_returnsSellerDetails() {
        String loginUsername = "seller_" + SELLER_EMAIL;

        when(sellerRepository.findByEmail(SELLER_EMAIL))
                .thenReturn(seller);
        when(seller.getEmail()).thenReturn(SELLER_EMAIL);
        when(seller.getPassword()).thenReturn(ENCODED_PASSWORD);
        when(seller.getRole()).thenReturn(USER_ROLE.ROLE_SELLER);

        UserDetails result =
                userDetailsService.loadUserByUsername(loginUsername);

        assertEquals(SELLER_EMAIL, result.getUsername());
        assertEquals(ENCODED_PASSWORD, result.getPassword());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SELLER")));

        verify(sellerRepository).findByEmail(SELLER_EMAIL);
        verifyNoInteractions(userRepository);
    }

    @Test
    void loadUserByUsername_whenCustomerRoleIsNull_defaultsToCustomerRole() {
        when(userRepository.findByEmail(CUSTOMER_EMAIL))
                .thenReturn(customer);
        when(customer.getEmail()).thenReturn(CUSTOMER_EMAIL);
        when(customer.getPassword()).thenReturn(ENCODED_PASSWORD);
        when(customer.getRole()).thenReturn(null);

        UserDetails result =
                userDetailsService.loadUserByUsername(CUSTOMER_EMAIL);

        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER")));
    }

    @Test
    void loadUserByUsername_whenCustomerDoesNotExist_throwsException() {
        when(userRepository.findByEmail(CUSTOMER_EMAIL))
                .thenReturn(null);

        assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(CUSTOMER_EMAIL)
        );
    }

    @Test
    void loadUserByUsername_whenSellerDoesNotExist_throwsException() {
        when(sellerRepository.findByEmail(SELLER_EMAIL))
                .thenReturn(null);

        assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(
                        "seller_" + SELLER_EMAIL
                )
        );
    }

    @Test
    void loadUserByUsername_whenUsernameIsNull_throwsException() {
        assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(null)
        );

        verifyNoInteractions(userRepository, sellerRepository);
    }

    @Test
    void loadUserByUsername_whenUsernameIsBlank_throwsException() {
        assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("   ")
        );

        verifyNoInteractions(userRepository, sellerRepository);
    }
}
