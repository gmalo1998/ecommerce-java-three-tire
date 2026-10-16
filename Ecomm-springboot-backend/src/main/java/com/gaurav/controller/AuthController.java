package com.gaurav.controller;

import com.gaurav.domain.USER_ROLE;
import com.gaurav.exception.SellerException;
import com.gaurav.exception.UserException;
import com.gaurav.model.VerificationCode;
import com.gaurav.request.LoginRequest;
import com.gaurav.request.SignupRequest;
import com.gaurav.response.ApiResponse;
import com.gaurav.response.AuthResponse;
import com.gaurav.service.AuthService;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sent/login-signup-otp")
    public ResponseEntity<ApiResponse> sentLoginOtp(
            @RequestBody VerificationCode req)
            throws MessagingException, UserException {

        log.info("Login/signup OTP request received");

        authService.sentLoginOtp(req.getEmail());

        ApiResponse response = new ApiResponse();
        response.setMessage("otp sent");

        log.info("Login/signup OTP request processed successfully");

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> createUserHandler(
            @Valid @RequestBody SignupRequest req)
            throws SellerException {

        log.info("User signup request received");

        String token = authService.createUser(req);

        AuthResponse response = new AuthResponse();
        response.setJwt(token);
        response.setMessage("Register Success");
        response.setRole(USER_ROLE.ROLE_CUSTOMER);

        log.info("User signup completed successfully");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signin(
            @RequestBody LoginRequest loginRequest)
            throws SellerException {

        log.info("User signin request received");

        AuthResponse response = authService.signin(loginRequest);

        log.info("Signin request processed successfully");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}