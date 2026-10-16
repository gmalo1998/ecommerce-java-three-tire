
package com.gaurav.service.impl;

import com.gaurav.config.JwtProvider;
import com.gaurav.exception.UserException;
import com.gaurav.model.User;
import com.gaurav.repository.PasswordResetTokenRepository;
import com.gaurav.repository.UserRepository;
import com.gaurav.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImplementation implements UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JavaMailSender javaMailSender;

    @Override
    @Transactional(readOnly = true)
    public User findUserProfileByJwt(String jwt) throws UserException {

        if (jwt == null || jwt.isBlank()) {
            log.warn("User profile lookup rejected: JWT is missing");
            throw new IllegalArgumentException("JWT must not be blank");
        }

        log.debug("Extracting email from JWT for user profile lookup");

        String email = jwtProvider.getEmailFromJwtToken(jwt);

        if (email == null || email.isBlank()) {
            log.warn("User profile lookup failed: JWT did not provide an email");
            throw new UserException("Unable to identify user from JWT");
        }

        User user = userRepository.findByEmail(email);

        if (user == null) {
            log.warn("User profile lookup failed: no user found for JWT email");
            throw new UserException("User does not exist");
        }

        log.debug("User profile retrieved successfully");
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public User findUserByEmail(String username) throws UserException {

        if (username == null || username.isBlank()) {
            log.warn("User lookup rejected: email is missing");
            throw new IllegalArgumentException("Email must not be blank");
        }

        log.debug("Looking up user by email");

        User user = userRepository.findByEmail(username);

        if (user == null) {
            log.warn("User lookup failed: no user found for requested email");
            throw new UserException("User does not exist");
        }

        log.debug("User found successfully");
        return user;
    }
}

