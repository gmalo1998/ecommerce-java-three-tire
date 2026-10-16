
package com.gaurav.service.impl;

import com.gaurav.exception.CouponNotValidException;
import com.gaurav.model.Cart;
import com.gaurav.model.Coupon;
import com.gaurav.model.User;
import com.gaurav.repository.CartRepository;
import com.gaurav.repository.CouponRepository;
import com.gaurav.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceImplTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CouponServiceImpl couponService;

    private Coupon coupon;
    private Cart cart;
    private User user;

    @BeforeEach
    void setUp() {
        coupon = new Coupon();
        coupon.setId(1L);
        coupon.setCode("SAVE20");
        coupon.setDiscountPercentage(20.0);
        coupon.setMinimumOrderValue(500.0);
        coupon.setValidityStartDate(LocalDate.now().minusDays(1));
        coupon.setValidityEndDate(LocalDate.now().plusDays(7));
        coupon.setActive(true);

        cart = new Cart();
        cart.setId(1L);
        cart.setTotalSellingPrice(1000.0);
        cart.setCouponPrice(0);

        user = new User();
        user.setId(1L);
    }

    @Test
    void testApplyCoupon() throws Exception {
        String code = "SAVE20";

        when(couponRepository.findByCode(code))
                .thenReturn(coupon);

        when(cartRepository.findByUserId(user.getId()))
                .thenReturn(cart);

        when(cartRepository.save(cart))
                .thenReturn(cart);

        Cart result = couponService.applyCoupon(code, 1000.0, user);

        assertNotNull(result);
        assertEquals(800.0, cart.getTotalSellingPrice());
        assertEquals(200, cart.getCouponPrice());
        assertEquals(code, cart.getCouponCode());
        assertTrue(user.getUsedCoupons().contains(coupon));

        verify(userRepository).save(user);
        verify(cartRepository).save(cart);
    }

    @Test
    void testApplyCouponWhenCouponDoesNotExist() {
        String code = "INVALID";

        when(couponRepository.findByCode(code))
                .thenReturn(null);

        assertThrows(
                CouponNotValidException.class,
                () -> couponService.applyCoupon(code, 1000.0, user)
        );

        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void testApplyCouponWhenCartDoesNotExist() {
        when(couponRepository.findByCode("SAVE20"))
                .thenReturn(coupon);

        when(cartRepository.findByUserId(user.getId()))
                .thenReturn(null);

        assertThrows(
                CouponNotValidException.class,
                () -> couponService.applyCoupon("SAVE20", 1000.0, user)
        );
    }

    @Test
    void testApplyCouponWhenOrderIsBelowMinimumValue() {
        when(couponRepository.findByCode("SAVE20"))
                .thenReturn(coupon);

        when(cartRepository.findByUserId(user.getId()))
                .thenReturn(cart);

        assertThrows(
                CouponNotValidException.class,
                () -> couponService.applyCoupon("SAVE20", 400.0, user)
        );

        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void testApplyCouponWhenCouponIsInactive() {
        coupon.setActive(false);

        when(couponRepository.findByCode("SAVE20"))
                .thenReturn(coupon);

        when(cartRepository.findByUserId(user.getId()))
                .thenReturn(cart);

        assertThrows(
                CouponNotValidException.class,
                () -> couponService.applyCoupon("SAVE20", 1000.0, user)
        );
    }

    @Test
    void testApplyCouponWhenCouponWasAlreadyUsed() {
        user.getUsedCoupons().add(coupon);

        when(couponRepository.findByCode("SAVE20"))
                .thenReturn(coupon);

        when(cartRepository.findByUserId(user.getId()))
                .thenReturn(cart);

        assertThrows(
                CouponNotValidException.class,
                () -> couponService.applyCoupon("SAVE20", 1000.0, user)
        );
    }

    @Test
    void testRemoveCoupon() throws Exception {
        user.getUsedCoupons().add(coupon);

        cart.setTotalSellingPrice(800.0);
        cart.setCouponPrice(200);
        cart.setCouponCode("SAVE20");

        when(couponRepository.findByCode("SAVE20"))
                .thenReturn(coupon);

        when(cartRepository.findByUserId(user.getId()))
                .thenReturn(cart);

        when(cartRepository.save(cart))
                .thenReturn(cart);

        Cart result = couponService.removeCoupon("SAVE20", user);

        assertNotNull(result);
        assertEquals(1000.0, cart.getTotalSellingPrice());
        assertEquals(0, cart.getCouponPrice());
        assertNull(cart.getCouponCode());
        assertFalse(user.getUsedCoupons().contains(coupon));

        verify(userRepository).save(user);
        verify(cartRepository).save(cart);
    }

    @Test
    void testRemoveCouponWhenCouponDoesNotExist() {
        when(couponRepository.findByCode("INVALID"))
                .thenReturn(null);

        assertThrows(
                CouponNotValidException.class,
                () -> couponService.removeCoupon("INVALID", user)
        );
    }

    @Test
    void testRemoveCouponWhenCouponIsNotAppliedToCart() {
        when(couponRepository.findByCode("SAVE20"))
                .thenReturn(coupon);

        when(cartRepository.findByUserId(user.getId()))
                .thenReturn(cart);

        assertThrows(
                CouponNotValidException.class,
                () -> couponService.removeCoupon("SAVE20", user)
        );
    }
}

