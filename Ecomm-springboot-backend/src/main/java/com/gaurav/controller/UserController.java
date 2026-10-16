
package com.gaurav.controller;

import com.gaurav.exception.UserException;
import com.gaurav.model.User;
import com.gaurav.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfileHandler(
            @RequestHeader("Authorization") String jwt)
            throws UserException {

        log.info("Received request to fetch authenticated user's profile");

        User user = userService.findUserProfileByJwt(jwt);

        log.info("Successfully fetched authenticated user's profile");

        return new ResponseEntity<>(user, HttpStatus.OK);
    }
}

