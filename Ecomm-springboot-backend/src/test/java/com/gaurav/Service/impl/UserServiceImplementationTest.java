
package com.gaurav.service.impl;

import com.gaurav.config.JwtProvider;
import com.gaurav.exception.UserException;
import com.gaurav.model.User;
import com.gaurav.repository.PasswordResetTokenRepository;
import com.gaurav.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplementationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private UserServiceImplementation userService;

    private User user;

    private static final String EMAIL = "user@example.com";
    private static final String JWT = "valid-test-jwt";

    @BeforeEach
    void setUp() {
        user = mock(User.class);
    }

    // FIND USER PROFILE BY JWT

    @Test
    void findUserProfileByJwt_shouldReturnUser() throws Exception {
        when(jwtProvider.getEmailFromJwtToken(JWT)).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(user);

        User result = userService.findUserProfileByJwt(JWT);

        assertSame(user, result);

        verify(jwtProvider).getEmailFromJwtToken(JWT);
        verify(userRepository).findByEmail(EMAIL);
    }

    @Test
    void findUserProfileByJwt_shouldRejectNullJwt() {
        assertThrows(
                IllegalArgumentException.class,
                () -> userService.findUserProfileByJwt(null)
        );

        verifyNoInteractions(jwtProvider, userRepository);
    }

    @Test
    void findUserProfileByJwt_shouldRejectBlankJwt() {
        assertThrows(
                IllegalArgumentException.class,
                () -> userService.findUserProfileByJwt("  ")
        );

        verifyNoInteractions(jwtProvider, userRepository);
    }

    @Test
    void findUserProfileByJwt_shouldThrowWhenJwtHasNoEmail()
            throws Exception {
        when(jwtProvider.getEmailFromJwtToken(JWT)).thenReturn(null);

        assertThrows(
                UserException.class,
                () -> userService.findUserProfileByJwt(JWT)
        );

        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void findUserProfileByJwt_shouldThrowWhenUserDoesNotExist()
            throws Exception {
        when(jwtProvider.getEmailFromJwtToken(JWT)).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(null);

        assertThrows(
                UserException.class,
                () -> userService.findUserProfileByJwt(JWT)
        );

        verify(userRepository).findByEmail(EMAIL);
    }

    // FIND USER BY EMAIL

    @Test
    void findUserByEmail_shouldReturnUser() throws Exception {
        when(userRepository.findByEmail(EMAIL)).thenReturn(user);

        User result = userService.findUserByEmail(EMAIL);

        assertSame(user, result);

        verify(userRepository).findByEmail(EMAIL);
    }

    @Test
    void findUserByEmail_shouldThrowWhenUserDoesNotExist() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(null);

        assertThrows(
                UserException.class,
                () -> userService.findUserByEmail(EMAIL)
        );

        verify(userRepository).findByEmail(EMAIL);
    }

    @Test
    void findUserByEmail_shouldRejectNullEmail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> userService.findUserByEmail(null)
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void findUserByEmail_shouldRejectBlankEmail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> userService.findUserByEmail(" ")
        );

        verifyNoInteractions(userRepository);
    }
}

