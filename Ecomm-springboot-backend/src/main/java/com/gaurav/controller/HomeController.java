
package com.gaurav.controller;

import com.gaurav.response.ApiResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HomeController {

    @GetMapping
    public ResponseEntity<ApiResponse> home() {

        log.debug("Home endpoint requested");

        ApiResponse response = new ApiResponse();
        response.setMessage("Ecommerce multi vendor system");

        log.debug("Home endpoint response prepared successfully");

        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }
}