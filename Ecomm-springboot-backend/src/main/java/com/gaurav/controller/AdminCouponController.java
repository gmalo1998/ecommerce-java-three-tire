package com.gaurav.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gaurav.model.Cart;
import com.gaurav.model.Coupon;
import com.gaurav.model.User;
import com.gaurav.service.CouponService;
import com.gaurav.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class AdminCouponController {

    private static final Logger logger =
            LoggerFactory.getLogger(AdminCouponController.class);

    private final CouponService couponService;
    private final UserService userService;

    @PostMapping("/apply")
    public ResponseEntity<Cart> applyCoupon(
            @RequestParam String apply,
            @RequestParam String code,
            @RequestParam double orderValue,
            @RequestHeader("Authorization") String jwt)
            throws Exception {

        if (!"true".equalsIgnoreCase(apply)
                && !"false".equalsIgnoreCase(apply)) {
            throw new IllegalArgumentException(
                    "The apply parameter must be true or false");
        }

        if (!Double.isFinite(orderValue) || orderValue < 0) {
            throw new IllegalArgumentException(
                    "Order value must be a non-negative finite number");
        }

        User user = userService.findUserProfileByJwt(jwt);

        Cart cart;

        if ("true".equalsIgnoreCase(apply)) {
            cart = couponService.applyCoupon(code, orderValue, user);
            logger.info("Coupon application processed successfully");
        } else {
            cart = couponService.removeCoupon(code, user);
            logger.info("Coupon removal processed successfully");
        }

        return ResponseEntity.ok(cart);
    }

    // Admin operations: protect these endpoints with ROLE_ADMIN.

    @PostMapping("/admin/create")
    public ResponseEntity<Coupon> createCoupon(
            @RequestBody Coupon coupon) {

        Coupon createdCoupon = couponService.createCoupon(coupon);

        logger.info("Coupon created successfully");

        return ResponseEntity.ok(createdCoupon);
    }

    @DeleteMapping("/admin/delete/{id}")
    public ResponseEntity<String> deleteCoupon(
            @PathVariable Long id) {

        couponService.deleteCoupon(id);

        logger.info("Coupon deleted successfully. Coupon ID: {}", id);

        return ResponseEntity.ok("Coupon deleted successfully");
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<Coupon>> getAllCoupons() {

        List<Coupon> coupons = couponService.getAllCoupons();

        logger.debug("Retrieved {} coupons", coupons.size());

        return ResponseEntity.ok(coupons);
    }
}