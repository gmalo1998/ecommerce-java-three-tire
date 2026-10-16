
package com.gaurav.service.impl;

import com.gaurav.model.VerificationCode;
import com.gaurav.repository.VerificationCodeRepository;
import com.gaurav.service.VerificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationServiceImpl implements VerificationService {

    private final VerificationCodeRepository verificationCodeRepository;

    @Override
    @Transactional
    public VerificationCode createVerificationCode(String otp, String email) {

        if (otp == null || otp.isBlank()) {
            log.warn("Verification code creation rejected: OTP is missing");
            throw new IllegalArgumentException("OTP must not be blank");
        }

        if (email == null || email.isBlank()) {
            log.warn("Verification code creation rejected: email is missing");
            throw new IllegalArgumentException("Email must not be blank");
        }

        log.debug("Checking for an existing verification code");

        VerificationCode existingCode =
                verificationCodeRepository.findByEmail(email);

        if (existingCode != null) {
            verificationCodeRepository.delete(existingCode);
            log.debug("Existing verification code replaced");
        }

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setOtp(otp);
        verificationCode.setEmail(email);

        VerificationCode savedCode =
                verificationCodeRepository.save(verificationCode);

        log.info("Verification code created successfully for the requested email");

        return savedCode;
    }
}

