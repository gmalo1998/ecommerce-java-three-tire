
package com.gaurav.controller;

import com.gaurav.config.JwtProvider;
import com.gaurav.domain.AccountStatus;
import com.gaurav.domain.USER_ROLE;
import com.gaurav.exception.SellerException;
import com.gaurav.model.Seller;
import com.gaurav.model.SellerReport;
import com.gaurav.model.VerificationCode;
import com.gaurav.repository.VerificationCodeRepository;
import com.gaurav.response.ApiResponse;
import com.gaurav.response.AuthResponse;
import com.gaurav.service.EmailService;
import com.gaurav.service.SellerReportService;
import com.gaurav.service.SellerService;
import com.gaurav.service.VerificationService;
import com.gaurav.service.impl.CustomeUserServiceImplementation;
import com.gaurav.utils.OtpUtils;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;
    private final SellerReportService sellerReportService;
    private final EmailService emailService;
    private final VerificationCodeRepository verificationCodeRepository;
    private final VerificationService verificationService;
    private final JwtProvider jwtProvider;
    private final CustomeUserServiceImplementation customeUserServiceImplementation;

    @PostMapping("/sent/login-top")
    public ResponseEntity<ApiResponse> sentLoginOtp(
            @RequestBody VerificationCode req)
            throws MessagingException, SellerException {

        String email = req.getEmail();
        log.info("Seller login OTP requested.");

        sellerService.getSellerByEmail(email);

        String otp = OtpUtils.generateOTP();
        VerificationCode verificationCode =
                verificationService.createVerificationCode(otp, email);

        emailService.sendVerificationOtpEmail(
                email,
                verificationCode.getOtp(),
                "Shopzy Login Otp",
                "Your login OTP is - "
        );

        ApiResponse response = new ApiResponse();
        response.setMessage("otp sent");

        log.info("Seller login OTP email dispatch requested.");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/verify/login-top")
    public ResponseEntity<AuthResponse> verifyLoginOtp(
            @RequestBody VerificationCode req)
            throws MessagingException, SellerException {

        String email = req.getEmail();
        String otp = req.getOtp();

        log.debug("Verifying seller login OTP.");

        VerificationCode verificationCode =
                verificationCodeRepository.findByEmail(email);

        if (verificationCode == null
                || verificationCode.getOtp() == null
                || !verificationCode.getOtp().equals(otp)) {
            log.warn("Seller login OTP verification failed.");
            throw new SellerException("wrong otp...");
        }

        Authentication authentication = authenticate(email);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtProvider.generateToken(authentication);

        AuthResponse response = new AuthResponse();
        response.setMessage("Login Success");
        response.setJwt(token);

        Collection<? extends GrantedAuthority> authorities =
                authentication.getAuthorities();

        String roleName = authorities.isEmpty()
                ? null
                : authorities.iterator().next().getAuthority();

        if (roleName == null) {
            throw new BadCredentialsException("No role assigned to seller");
        }

        try {
            response.setRole(USER_ROLE.valueOf(roleName));
        } catch (IllegalArgumentException exception) {
            log.error("Unexpected authority configured for seller login.");
            throw new BadCredentialsException("Invalid seller role");
        }

        log.info("Seller login successful.");
        return ResponseEntity.ok(response);
    }

    private Authentication authenticate(String username) {
        UserDetails userDetails =
                customeUserServiceImplementation.loadUserByUsername(
                        "seller_" + username
                );

        if (userDetails == null) {
            log.warn("Seller authentication failed: user details not found.");
            throw new BadCredentialsException("Invalid username or password");
        }

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }

    @PatchMapping("/verify/{otp}")
    public ResponseEntity<Seller> verifySellerEmail(
            @PathVariable String otp) throws SellerException {

        log.debug("Seller email verification requested.");

        VerificationCode verificationCode =
                verificationCodeRepository.findByOtp(otp);

        if (verificationCode == null
                || verificationCode.getOtp() == null
                || !verificationCode.getOtp().equals(otp)) {
            log.warn("Seller email verification failed.");
            throw new SellerException("wrong otp...");
        }

        Seller seller = sellerService.verifyEmail(
                verificationCode.getEmail(),
                otp
        );

        log.info("Seller email verification completed.");
        return ResponseEntity.ok(seller);
    }

    @PostMapping
    public ResponseEntity<Seller> createSeller(
            @RequestBody Seller seller)
            throws SellerException, MessagingException {

        log.info("Seller registration requested.");

        Seller savedSeller = sellerService.createSeller(seller);

        String otp = OtpUtils.generateOTP();
        VerificationCode verificationCode =
                verificationService.createVerificationCode(
                        otp,
                        savedSeller.getEmail()
                );

        emailService.sendVerificationOtpEmail(
                savedSeller.getEmail(),
                verificationCode.getOtp(),
                "Shopzy Email Verification Code",
                "Welcome to Shopzy, verify your account using this link "
                        + "http://localhost:3000/verify-seller/"
        );

        log.info("Seller registration completed; verification email dispatch requested.");
        return new ResponseEntity<>(savedSeller, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Seller> getSellerById(
            @PathVariable Long id) throws SellerException {

        log.debug("Fetching seller by ID: {}", id);

        Seller seller = sellerService.getSellerById(id);
        return ResponseEntity.ok(seller);
    }

    @GetMapping("/profile")
    public ResponseEntity<Seller> getSellerByJwt(
            @RequestHeader("Authorization") String jwt)
            throws SellerException {

        String email = jwtProvider.getEmailFromJwtToken(jwt);
        Seller seller = sellerService.getSellerByEmail(email);

        log.debug("Seller profile retrieved.");
        return ResponseEntity.ok(seller);
    }

    @GetMapping("/report")
    public ResponseEntity<SellerReport> getSellerReport(
            @RequestHeader("Authorization") String jwt)
            throws SellerException {

        String email = jwtProvider.getEmailFromJwtToken(jwt);
        Seller seller = sellerService.getSellerByEmail(email);
        SellerReport report = sellerReportService.getSellerReport(seller);

        log.debug("Seller report retrieved.");
        return ResponseEntity.ok(report);
    }

    @GetMapping
    public ResponseEntity<List<Seller>> getAllSellers(
            @RequestParam(required = false) AccountStatus status) {

        log.debug("Fetching sellers. Status filter provided: {}", status != null);

        List<Seller> sellers = sellerService.getAllSellers(status);
        return ResponseEntity.ok(sellers);
    }

    @PatchMapping
    public ResponseEntity<Seller> updateSeller(
            @RequestHeader("Authorization") String jwt,
            @RequestBody Seller seller) throws SellerException {

        Seller profile = sellerService.getSellerProfile(jwt);
        Seller updatedSeller = sellerService.updateSeller(profile.getId(), seller);

        log.info("Seller profile updated.");
        return ResponseEntity.ok(updatedSeller);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSeller(
            @PathVariable Long id) throws SellerException {

        sellerService.deleteSeller(id);

        log.info("Seller deletion completed for ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}