
package com.gaurav.service.impl;

import com.gaurav.exception.CouponNotValidException;
import com.gaurav.model.Cart;
import com.gaurav.model.Coupon;
import com.gaurav.model.User;
import com.gaurav.repository.CartRepository;
import com.gaurav.repository.CouponRepository;
import com.gaurav.repository.UserRepository;
import com.gaurav.service.CouponService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;

    @Override
    @Transactional
    public Cart applyCoupon(String code, double orderValue, User user)
            throws Exception {

        log.info("Request received to apply coupon for user ID {}",
                user.getId());

        Coupon coupon = couponRepository.findByCode(code);

        if (coupon == null) {
            throw new CouponNotValidException("Coupon not found");
        }

        Cart cart = cartRepository.findByUserId(user.getId());

        if (cart == null) {
            throw new CouponNotValidException("Cart not found");
        }

        if (user.getUsedCoupons().contains(coupon)) {
            throw new CouponNotValidException("Coupon already used");
        }

        if (orderValue < coupon.getMinimumOrderValue()) {
            throw new CouponNotValidException(
                    "Minimum order value is "
                            + coupon.getMinimumOrderValue()
            );
        }

        LocalDate today = LocalDate.now();

        if (!coupon.isActive()
                || today.isBefore(coupon.getValidityStartDate())
                || today.isAfter(coupon.getValidityEndDate())) {

            throw new CouponNotValidException("Coupon is not valid");
        }

        if (cart.getCouponPrice() > 0) {
            throw new CouponNotValidException(
                    "Remove the currently applied coupon first"
            );
        }

        double currentTotal = cart.getTotalSellingPrice();

        double discount = Math.round(
                currentTotal * coupon.getDiscountPercentage() / 100.0
        );

        // A discount must not reduce the cart total below zero.
        discount = Math.min(discount, currentTotal);

        cart.setTotalSellingPrice((int) (currentTotal - discount));
        cart.setCouponCode(code);
        cart.setCouponPrice((int) discount);

        /*
         * Preserving your existing behavior: the coupon is recorded as used
         * when applied. For a checkout flow, it is usually better to mark it
         * as used only after the order is successfully placed.
         */
        user.getUsedCoupons().add(coupon);
        userRepository.save(user);

        Cart updatedCart = cartRepository.save(cart);

        log.info("Coupon applied successfully for user ID {}",
                user.getId());

        return updatedCart;
    }

    @Override
    @Transactional
    public Cart removeCoupon(String code, User user) throws Exception {

        log.info("Request received to remove coupon for user ID {}",
                user.getId());

        Coupon coupon = couponRepository.findByCode(code);

        if (coupon == null) {
            throw new CouponNotValidException("Coupon not found");
        }

        Cart cart = cartRepository.findByUserId(user.getId());

        if (cart == null) {
            throw new CouponNotValidException("Cart not found");
        }

        // Only remove a coupon if it is the one currently applied.
        if (!code.equals(cart.getCouponCode())) {
            throw new CouponNotValidException(
                    "This coupon is not applied to the cart"
            );
        }

        cart.setTotalSellingPrice(
                cart.getTotalSellingPrice() + cart.getCouponPrice()
        );
        cart.setCouponCode(null);
        cart.setCouponPrice(0);

        user.getUsedCoupons().remove(coupon);
        userRepository.save(user);

        Cart updatedCart = cartRepository.save(cart);

        log.info("Coupon removed successfully for user ID {}",
                user.getId());

        return updatedCart;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Coupon createCoupon(Coupon coupon) {

        log.info("Admin requested coupon creation");

        Coupon savedCoupon = couponRepository.save(coupon);

        log.info("Coupon created successfully with ID {}",
                savedCoupon.getId());

        return savedCoupon;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCoupon(Long couponId) {

        log.info("Admin requested deletion of coupon ID {}", couponId);

        if (!couponRepository.existsById(couponId)) {
            throw new IllegalArgumentException("Coupon not found");
        }

        couponRepository.deleteById(couponId);

        log.info("Coupon ID {} deleted successfully", couponId);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<Coupon> getAllCoupons() {

        log.info("Admin requested all coupons");

        return couponRepository.findAll();
    }

    @Override
    public Coupon getCouponById(Long couponId) {

        log.debug("Fetching coupon ID {}", couponId);

        return couponRepository.findById(couponId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Coupon not found")
                );
    }
}

