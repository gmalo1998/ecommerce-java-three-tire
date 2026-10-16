
package com.gaurav.controller;

import com.gaurav.service.CartItemService;
import com.gaurav.service.UserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class CartItemControllerTest {

    @Mock
    private CartItemService cartItemService;

    @Mock
    private UserService userService;

    @InjectMocks
    private CartItemController cartItemController;

    @Test
    void controllerShouldBeCreatedWithDependencies() {
        assertNotNull(cartItemController);
    }
}