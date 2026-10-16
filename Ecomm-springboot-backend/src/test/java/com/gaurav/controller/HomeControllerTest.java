
package com.gaurav.controller;

import com.gaurav.response.ApiResponse;

import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HomeControllerTest {

    private final HomeController homeController = new HomeController();

    @Test
    void home_shouldReturnWelcomeMessage() {

        ResponseEntity<ApiResponse> response = homeController.home();

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(
                "Ecommerce multi vendor system",
                response.getBody().getMessage()
        );
    }
}