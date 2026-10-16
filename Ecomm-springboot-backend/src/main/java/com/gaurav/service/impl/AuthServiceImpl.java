
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
import com.gaurav.service.AuthService;
import com.gaurav.service.EmailService;
import com.gaurav.service.UserService;
import com.gaurav.utils.OtpUtils;

import jakarta.mail.MessagingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final CustomeUserServiceImplementation customUserDetails;
    private final CartRepository cartRepository;

    @Override
    public void sentLoginOtp(String email)
            throws UserException, MessagingException {

        log.info("OTP request received");

        String signingPrefix = "signing_";

        if (email.startsWith(signingPrefix)) {
            email = email.substring(signingPrefix.length());
            userService.findUserByEmail(email);
        }

        VerificationCode existingCode =
                verificationCodeRepository.findByEmail(email);

        if (existingCode != null) {
            verificationCodeRepository.delete(existingCode);
        }

        String otp = OtpUtils.generateOTP();

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setOtp(otp);
        verificationCode.setEmail(email);

        verificationCodeRepository.save(verificationCode);

        String subject = "Shopzy Login/Signup OTP";
        String text = "Your login OTP is - ";

        emailService.sendVerificationOtpEmail(
                email, otp, subject, text
        );

        log.info("OTP generated and email dispatch completed");
    }

    @Override
    public String createUser(SignupRequest req) throws SellerException {

        String email = req.getEmail();
        String fullName = req.getFullName();
        String otp = req.getOtp();

        VerificationCode verificationCode =
                verificationCodeRepository.findByEmail(email);

        if (verificationCode == null
                || !verificationCode.getOtp().equals(otp)) {

            log.warn("Signup rejected because OTP verification failed");
            throw new SellerException("Wrong OTP");
        }

        User user = userRepository.findByEmail(email);

        if (user == null) {

            User createdUser = new User();
            createdUser.setEmail(email);
            createdUser.setFullName(fullName);
            createdUser.setRole(USER_ROLE.ROLE_CUSTOMER);

            // Replace this placeholder with a verified mobile number
            // collected during registration, if your model requires it.
            createdUser.setMobile("9083476123");

            // Do not store the OTP as the account password.
            // This random password is not disclosed to the user.
            String randomPassword = UUID.randomUUID().toString();
            createdUser.setPassword(
                    passwordEncoder.encode(randomPassword)
            );

            user = userRepository.save(createdUser);

            Cart cart = new Cart();
            cart.setUser(user);
            cartRepository.save(cart);

            log.info("New customer account created successfully");
        } else {
            log.info("Existing customer account verified successfully");
        }

        // Make the OTP single-use.
        verificationCodeRepository.delete(verificationCode);

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(
                new SimpleGrantedAuthority(
                        USER_ROLE.ROLE_CUSTOMER.toString()
                )
        );

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        email, null, authorities
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        String token = jwtProvider.generateToken(authentication);

        log.info("Signup authentication completed successfully");

        return token;
    }

    @Override
    public AuthResponse signin(LoginRequest req) throws SellerException {

        String username = req.getEmail();
        String otp = req.getOtp();

        log.info("Sign-in request received");

        Authentication authentication = authenticate(username, otp);

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        String token = jwtProvider.generateToken(authentication);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setMessage("Login Success");
        authResponse.setJwt(token);

        Collection<? extends GrantedAuthority> authorities =
                authentication.getAuthorities();

        String roleName = authorities.isEmpty()
                ? null
                : authorities.iterator().next().getAuthority();

        if (roleName == null) {
            log.error("Authentication completed without an assigned role");
            throw new BadCredentialsException("User role is missing");
        }

        authResponse.setRole(USER_ROLE.valueOf(roleName));

        log.info("Sign-in completed successfully");

        return authResponse;
    }

    private Authentication authenticate(String username, String otp)
            throws SellerException {

        UserDetails userDetails =
                customUserDetails.loadUserByUsername(username);

        if (userDetails == null) {
            log.warn("Sign-in rejected because user details were not found");
            throw new BadCredentialsException(
                    "Invalid username or password"
            );
        }

        VerificationCode verificationCode =
                verificationCodeRepository.findByEmail(username);

        if (verificationCode == null
                || !verificationCode.getOtp().equals(otp)) {

            log.warn("Sign-in rejected because OTP verification failed");
            throw new SellerException("Wrong OTP");
        }

        // Delete the OTP only after successful verification.
        verificationCodeRepository.delete(verificationCode);

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }
}

