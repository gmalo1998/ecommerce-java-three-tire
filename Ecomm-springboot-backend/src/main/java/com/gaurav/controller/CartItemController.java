
package com.gaurav.controller;

import com.gaurav.service.CartItemService;
import com.gaurav.service.UserService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/cart_items")
public class CartItemController {

    private final CartItemService cartItemService;
    private final UserService userService;

    public CartItemController(
            CartItemService cartItemService,
            UserService userService) {

        this.cartItemService = cartItemService;
        this.userService = userService;

        log.debug("CartItemController initialized");
    }
}