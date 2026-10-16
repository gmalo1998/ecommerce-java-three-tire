
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private static final String TEST_TOKEN = "test-jwt-token";

    @Test
    void sentLoginOtp_shouldReturnCreatedResponse()
            throws MessagingException, UserException {

        VerificationCode request = new VerificationCode();
        request.setEmail("test@example.com");

        ResponseEntity<ApiResponse> response =
                authController.sentLoginOtp(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("otp sent", response.getBody().getMessage());

        verify(authService).sentLoginOtp("test@example.com");
    }

    @Test
    void createUserHandler_shouldReturnAuthResponse()
            throws SellerException {

        SignupRequest request = new SignupRequest();

        when(authService.createUser(request)).thenReturn(TEST_TOKEN);

        ResponseEntity<AuthResponse> response =
                authController.createUserHandler(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(TEST_TOKEN, response.getBody().getJwt());
        assertEquals("Register Success", response.getBody().getMessage());
        assertEquals(
                USER_ROLE.ROLE_CUSTOMER,
                response.getBody().getRole()
        );

        verify(authService).createUser(request);
    }

    @Test
    void signin_shouldReturnServiceAuthResponse()
            throws SellerException {

        LoginRequest request = new LoginRequest();

        AuthResponse expectedResponse = new AuthResponse();
        expectedResponse.setJwt(TEST_TOKEN);
        expectedResponse.setMessage("Login Success");

        when(authService.signin(request)).thenReturn(expectedResponse);

        ResponseEntity<AuthResponse> response =
                authController.signin(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());

        verify(authService).signin(request);
    }
}