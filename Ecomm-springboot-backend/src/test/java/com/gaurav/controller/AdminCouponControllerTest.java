
package com.gaurav.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.gaurav.model.Cart;
import com.gaurav.model.Coupon;
import com.gaurav.model.User;
import com.gaurav.service.CouponService;
import com.gaurav.service.UserService;

@ExtendWith(MockitoExtension.class)
class AdminCouponControllerTest {

    @Mock
    private CouponService couponService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminCouponController controller;

    private User user;
    private Cart cart;
    private Coupon coupon;

    @BeforeEach
    void setUp() {
        user = org.mockito.Mockito.mock(User.class);
        cart = org.mockito.Mockito.mock(Cart.class);
        coupon = org.mockito.Mockito.mock(Coupon.class);
    }

    @Test
    void shouldApplyCoupon() throws Exception {
        String jwt = "Bearer test-token";

        when(userService.findUserProfileByJwt(jwt))
                .thenReturn(user);

        when(couponService.applyCoupon("SAVE10", 100.0, user))
                .thenReturn(cart);

        ResponseEntity<Cart> response = controller.applyCoupon(
                "true", "SAVE10", 100.0, jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(cart, response.getBody());

        verify(userService).findUserProfileByJwt(jwt);
        verify(couponService).applyCoupon("SAVE10", 100.0, user);
    }

    @Test
    void shouldRemoveCoupon() throws Exception {
        String jwt = "Bearer test-token";

        when(userService.findUserProfileByJwt(jwt))
                .thenReturn(user);

        when(couponService.removeCoupon("SAVE10", user))
                .thenReturn(cart);

        ResponseEntity<Cart> response = controller.applyCoupon(
                "false", "SAVE10", 100.0, jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(cart, response.getBody());

        verify(couponService).removeCoupon("SAVE10", user);
    }

    @Test
    void shouldRejectInvalidApplyParameter() {
        assertThrows(
                IllegalArgumentException.class,
                () -> controller.applyCoupon(
                        "yes", "SAVE10", 100.0, "Bearer test-token")
        );

        verifyNoInteractions(userService, couponService);
    }

    @Test
    void shouldRejectNegativeOrderValue() {
        assertThrows(
                IllegalArgumentException.class,
                () -> controller.applyCoupon(
                        "true", "SAVE10", -10.0, "Bearer test-token")
        );

        verifyNoInteractions(userService, couponService);
    }

    @Test
    void shouldCreateCoupon() {
        when(couponService.createCoupon(coupon)).thenReturn(coupon);

        ResponseEntity<Coupon> response =
                controller.createCoupon(coupon);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(coupon, response.getBody());

        verify(couponService).createCoupon(coupon);
    }

    @Test
    void shouldDeleteCoupon() {
        Long couponId = 5L;

        ResponseEntity<String> response =
                controller.deleteCoupon(couponId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Coupon deleted successfully", response.getBody());

        verify(couponService).deleteCoupon(couponId);
    }

    @Test
    void shouldReturnAllCoupons() {
        List<Coupon> coupons = Arrays.asList(coupon);

        when(couponService.getAllCoupons()).thenReturn(coupons);

        ResponseEntity<List<Coupon>> response =
                controller.getAllCoupons();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(coupons, response.getBody());

        verify(couponService).getAllCoupons();
    }
}