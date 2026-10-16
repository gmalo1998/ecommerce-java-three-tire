
package com.gaurav.service.impl;

import com.gaurav.config.JwtProvider;
import com.gaurav.domain.USER_ROLE;
import com.gaurav.exception.SellerException;
import com.gaurav.exception.UserException;
import com.gaurav.model.Cart;
import com.gaurav.model.User;
import com.gaurav.model.VerificationCode;
import com.gaurav.repository.CartRepository;
import com.gaurav.repository.UserRepository;
import com.gaurav.repository.VerificationCodeRepository;
import com.gaurav.request.LoginRequest;
import com.gaurav.request.SignupRequest;
import com.gaurav.response.AuthResponse;
import com.gaurav.service.EmailService;
import com.gaurav.service.UserService;

import jakarta.mail.MessagingException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private VerificationCodeRepository verificationCodeRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private CustomeUserServiceImplementation customUserDetails;

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void sentLoginOtpShouldSaveCodeAndSendEmail()
            throws Exception {

        String email = "test@example.com";

        when(verificationCodeRepository.findByEmail(email))
                .thenReturn(null);

        authService.sentLoginOtp(email);

        verify(verificationCodeRepository).save(
                argThat(code ->
                        email.equals(code.getEmail())
                                && code.getOtp() != null
                                && !code.getOtp().isBlank()
                )
        );

        verify(emailService).sendVerificationOtpEmail(
                eq(email),
                anyString(),
                eq("Shopzy Login/Signup OTP"),
                anyString()
        );
    }

    @Test
    void sentLoginOtpShouldReplaceExistingCode()
            throws Exception {

        String email = "test@example.com";
        VerificationCode existingCode = new VerificationCode();
        existingCode.setEmail(email);
        existingCode.setOtp("123456");

        when(verificationCodeRepository.findByEmail(email))
                .thenReturn(existingCode);

        authService.sentLoginOtp(email);

        verify(verificationCodeRepository).delete(existingCode);
        verify(verificationCodeRepository).save(any(VerificationCode.class));
        verify(emailService).sendVerificationOtpEmail(
                eq(email), anyString(), anyString(), anyString()
        );
    }

    @Test
    void createUserShouldCreateCustomerAndReturnJwt()
            throws Exception {

        SignupRequest request = mock(SignupRequest.class);
        when(request.getEmail()).thenReturn("test@example.com");
        when(request.getFullName()).thenReturn("Test User");
        when(request.getOtp()).thenReturn("123456");

        VerificationCode code = new VerificationCode();
        code.setEmail("test@example.com");
        code.setOtp("123456");

        when(verificationCodeRepository.findByEmail("test@example.com"))
                .thenReturn(code);
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(null);
        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded-random-password");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtProvider.generateToken(any(Authentication.class)))
                .thenReturn("test-jwt");

        String token = authService.createUser(request);

        assertEquals("test-jwt", token);

        verify(userRepository).save(argThat(user ->
                "test@example.com".equals(user.getEmail())
                        && "Test User".equals(user.getFullName())
                        && user.getRole() == USER_ROLE.ROLE_CUSTOMER
                        && "encoded-random-password".equals(user.getPassword())
        ));

        verify(cartRepository).save(any(Cart.class));
        verify(verificationCodeRepository).delete(code);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void createUserShouldRejectInvalidOtp() {

        SignupRequest request = mock(SignupRequest.class);
        when(request.getEmail()).thenReturn("test@example.com");
        when(request.getOtp()).thenReturn("wrong");

        VerificationCode code = new VerificationCode();
        code.setEmail("test@example.com");
        code.setOtp("123456");

        when(verificationCodeRepository.findByEmail("test@example.com"))
                .thenReturn(code);

        assertThrows(
                SellerException.class,
                () -> authService.createUser(request)
        );

        verify(userRepository, never()).save(any(User.class));
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void signinShouldReturnJwtAndRole()
            throws Exception {

        LoginRequest request = mock(LoginRequest.class);
        when(request.getEmail()).thenReturn("test@example.com");
        when(request.getOtp()).thenReturn("123456");

        UserDetails userDetails =
                org.springframework.security.core.userdetails.User
                        .withUsername("test@example.com")
                        .password("")
                        .authorities(
                                new SimpleGrantedAuthority(
                                        USER_ROLE.ROLE_CUSTOMER.toString()
                                )
                        )
                        .build();

        VerificationCode code = new VerificationCode();
        code.setEmail("test@example.com");
        code.setOtp("123456");

        when(customUserDetails.loadUserByUsername("test@example.com"))
                .thenReturn(userDetails);
        when(verificationCodeRepository.findByEmail("test@example.com"))
                .thenReturn(code);
        when(jwtProvider.generateToken(any(Authentication.class)))
                .thenReturn("test-jwt");

        AuthResponse response = authService.signin(request);

        assertEquals("test-jwt", response.getJwt());
        assertEquals("Login Success", response.getMessage());
        assertEquals(USER_ROLE.ROLE_CUSTOMER, response.getRole());

        verify(verificationCodeRepository).delete(code);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void signinShouldRejectInvalidOtp() {

        LoginRequest request = mock(LoginRequest.class);
        when(request.getEmail()).thenReturn("test@example.com");
        when(request.getOtp()).thenReturn("wrong");

        UserDetails userDetails =
                org.springframework.security.core.userdetails.User
                        .withUsername("test@example.com")
                        .password("")
                        .authorities(
                                new SimpleGrantedAuthority(
                                        USER_ROLE.ROLE_CUSTOMER.toString()
                                )
                        )
                        .build();

        VerificationCode code = new VerificationCode();
        code.setEmail("test@example.com");
        code.setOtp("123456");

        when(customUserDetails.loadUserByUsername("test@example.com"))
                .thenReturn(userDetails);
        when(verificationCodeRepository.findByEmail("test@example.com"))
                .thenReturn(code);

        assertThrows(
                SellerException.class,
                () -> authService.signin(request)
        );

        verify(jwtProvider, never())
                .generateToken(any(Authentication.class));
        verify(verificationCodeRepository, never()).delete(code);
    }

    @Test
    void signinShouldRejectWhenUserDetailsAreMissing() {

        LoginRequest request = mock(LoginRequest.class);
        when(request.getEmail()).thenReturn("missing@example.com");
        when(request.getOtp()).thenReturn("123456");

        when(customUserDetails.loadUserByUsername("missing@example.com"))
                .thenReturn(null);

        assertThrows(
                BadCredentialsException.class,
                () -> authService.signin(request)
        );

        verify(verificationCodeRepository, never())
                .findByEmail(anyString());
    }
}

