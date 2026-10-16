
package com.gaurav.service.impl;

import com.gaurav.model.VerificationCode;
import com.gaurav.repository.VerificationCodeRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationServiceImplTest {

    @Mock
    private VerificationCodeRepository verificationCodeRepository;

    @InjectMocks
    private VerificationServiceImpl verificationService;

    private static final String OTP = "123456";
    private static final String EMAIL = "user@example.com";

    private VerificationCode existingCode;

    @BeforeEach
    void setUp() {
        existingCode = new VerificationCode();
        existingCode.setOtp("654321");
        existingCode.setEmail(EMAIL);
    }

    @Test
    void createVerificationCode_shouldSaveNewCode() {
        when(verificationCodeRepository.findByEmail(EMAIL))
                .thenReturn(null);

        when(verificationCodeRepository.save(any(VerificationCode.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        VerificationCode result =
                verificationService.createVerificationCode(OTP, EMAIL);

        assertNotNull(result);
        assertEquals(OTP, result.getOtp());
        assertEquals(EMAIL, result.getEmail());

        verify(verificationCodeRepository).findByEmail(EMAIL);
        verify(verificationCodeRepository).save(any(VerificationCode.class));
        verify(verificationCodeRepository, never())
                .delete(any(VerificationCode.class));
    }

    @Test
    void createVerificationCode_shouldDeleteExistingCodeBeforeSaving() {
        when(verificationCodeRepository.findByEmail(EMAIL))
                .thenReturn(existingCode);

        when(verificationCodeRepository.save(any(VerificationCode.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        VerificationCode result =
                verificationService.createVerificationCode(OTP, EMAIL);

        assertNotNull(result);
        assertEquals(OTP, result.getOtp());
        assertEquals(EMAIL, result.getEmail());

        verify(verificationCodeRepository).delete(existingCode);
        verify(verificationCodeRepository).save(any(VerificationCode.class));

        // Verify the old code is deleted before the replacement is saved.
        var order = inOrder(verificationCodeRepository);
        order.verify(verificationCodeRepository).findByEmail(EMAIL);
        order.verify(verificationCodeRepository).delete(existingCode);
        order.verify(verificationCodeRepository)
                .save(any(VerificationCode.class));
    }

    @Test
    void createVerificationCode_shouldRejectNullOtp() {
        assertThrows(
                IllegalArgumentException.class,
                () -> verificationService.createVerificationCode(null, EMAIL)
        );

        verifyNoInteractions(verificationCodeRepository);
    }

    @Test
    void createVerificationCode_shouldRejectBlankOtp() {
        assertThrows(
                IllegalArgumentException.class,
                () -> verificationService.createVerificationCode(" ", EMAIL)
        );

        verifyNoInteractions(verificationCodeRepository);
    }

    @Test
    void createVerificationCode_shouldRejectNullEmail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> verificationService.createVerificationCode(OTP, null)
        );

        verifyNoInteractions(verificationCodeRepository);
    }

    @Test
    void createVerificationCode_shouldRejectBlankEmail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> verificationService.createVerificationCode(OTP, " ")
        );

        verifyNoInteractions(verificationCodeRepository);
    }

    @Test
    void createVerificationCode_shouldCreateNewEntity() {
        when(verificationCodeRepository.findByEmail(EMAIL))
                .thenReturn(null);

        when(verificationCodeRepository.save(any(VerificationCode.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        verificationService.createVerificationCode(OTP, EMAIL);

        ArgumentCaptor<VerificationCode> captor =
                ArgumentCaptor.forClass(VerificationCode.class);

        verify(verificationCodeRepository).save(captor.capture());

        VerificationCode savedCode = captor.getValue();

        assertEquals(OTP, savedCode.getOtp());
        assertEquals(EMAIL, savedCode.getEmail());
        assertNotSame(existingCode, savedCode);
    }
}

