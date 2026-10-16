
package com.gaurav.service.impl;

import com.gaurav.config.JwtProvider;
import com.gaurav.domain.AccountStatus;
import com.gaurav.exception.SellerException;
import com.gaurav.model.Address;
import com.gaurav.model.Seller;
import com.gaurav.repository.AddressRepository;
import com.gaurav.repository.SellerRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerServiceImplTest {

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private SellerServiceImpl sellerService;

    private Seller seller;
    private Seller existingSeller;
    private Address address;

    @BeforeEach
    void setUp() {
        seller = mock(Seller.class);
        existingSeller = mock(Seller.class);
        address = mock(Address.class);
    }

    // CREATE SELLER

    @Test
    void createSeller_shouldSaveSellerSuccessfully() throws Exception {
        when(seller.getEmail()).thenReturn("seller@example.com");
        when(seller.getPassword()).thenReturn("plainPassword");
        when(seller.getPickupAddress()).thenReturn(address);

        when(sellerRepository.findByEmail("seller@example.com"))
                .thenReturn(null);

        when(addressRepository.save(address)).thenReturn(address);

        when(passwordEncoder.encode("plainPassword"))
                .thenReturn("encodedPassword");

        when(sellerRepository.save(any(Seller.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Seller result = sellerService.createSeller(seller);

        assertNotNull(result);
        assertEquals("seller@example.com", result.getEmail());
        assertEquals("encodedPassword", result.getPassword());
        assertSame(address, result.getPickupAddress());

        verify(sellerRepository).findByEmail("seller@example.com");
        verify(addressRepository).save(address);
        verify(passwordEncoder).encode("plainPassword");
        verify(sellerRepository).save(any(Seller.class));
    }

    @Test
    void createSeller_shouldRejectNullSeller() {
        assertThrows(
                IllegalArgumentException.class,
                () -> sellerService.createSeller(null)
        );

        verifyNoInteractions(sellerRepository);
    }

    @Test
    void createSeller_shouldRejectBlankEmail() {
        when(seller.getEmail()).thenReturn(" ");

        assertThrows(
                IllegalArgumentException.class,
                () -> sellerService.createSeller(seller)
        );

        verifyNoInteractions(sellerRepository);
    }

    @Test
    void createSeller_shouldRejectBlankPassword() {
        when(seller.getEmail()).thenReturn("seller@example.com");
        when(seller.getPassword()).thenReturn(" ");

        assertThrows(
                IllegalArgumentException.class,
                () -> sellerService.createSeller(seller)
        );

        verifyNoInteractions(sellerRepository);
    }

    @Test
    void createSeller_shouldRejectMissingPickupAddress() {
        when(seller.getEmail()).thenReturn("seller@example.com");
        when(seller.getPassword()).thenReturn("plainPassword");
        when(seller.getPickupAddress()).thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> sellerService.createSeller(seller)
        );

        verifyNoInteractions(sellerRepository);
    }

    @Test
    void createSeller_shouldRejectDuplicateEmail() throws Exception {
        when(seller.getEmail()).thenReturn("seller@example.com");
        when(seller.getPassword()).thenReturn("plainPassword");
        when(seller.getPickupAddress()).thenReturn(address);

        when(sellerRepository.findByEmail("seller@example.com"))
                .thenReturn(existingSeller);

        assertThrows(
                SellerException.class,
                () -> sellerService.createSeller(seller)
        );

        verify(addressRepository, never()).save(any(Address.class));
        verify(sellerRepository, never()).save(any(Seller.class));
    }

    // GET SELLER BY ID

    @Test
    void getSellerById_shouldReturnSeller() throws Exception {
        when(sellerRepository.findById(1L))
                .thenReturn(Optional.of(existingSeller));

        Seller result = sellerService.getSellerById(1L);

        assertSame(existingSeller, result);
        verify(sellerRepository).findById(1L);
    }

    @Test
    void getSellerById_shouldThrowWhenSellerDoesNotExist() {
        when(sellerRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                SellerException.class,
                () -> sellerService.getSellerById(99L)
        );
    }

    @Test
    void getSellerById_shouldRejectNullId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> sellerService.getSellerById(null)
        );

        verifyNoInteractions(sellerRepository);
    }

    // GET SELLER BY EMAIL

    @Test
    void getSellerByEmail_shouldReturnSeller() throws Exception {
        when(sellerRepository.findByEmail("seller@example.com"))
                .thenReturn(existingSeller);

        Seller result =
                sellerService.getSellerByEmail("seller@example.com");

        assertSame(existingSeller, result);
        verify(sellerRepository).findByEmail("seller@example.com");
    }

    @Test
    void getSellerByEmail_shouldThrowWhenSellerDoesNotExist() {
        when(sellerRepository.findByEmail("missing@example.com"))
                .thenReturn(null);

        assertThrows(
                SellerException.class,
                () -> sellerService.getSellerByEmail("missing@example.com")
        );
    }

    @Test
    void getSellerByEmail_shouldRejectBlankEmail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> sellerService.getSellerByEmail(" ")
        );

        verifyNoInteractions(sellerRepository);
    }

    // GET SELLER PROFILE

    @Test
    void getSellerProfile_shouldReturnSellerFromJwt() throws Exception {
        when(jwtProvider.getEmailFromJwtToken("valid-jwt"))
                .thenReturn("seller@example.com");

        when(sellerRepository.findByEmail("seller@example.com"))
                .thenReturn(existingSeller);

        Seller result = sellerService.getSellerProfile("valid-jwt");

        assertSame(existingSeller, result);
        verify(jwtProvider).getEmailFromJwtToken("valid-jwt");
    }

    @Test
    void getSellerProfile_shouldRejectBlankJwt() {
        assertThrows(
                IllegalArgumentException.class,
                () -> sellerService.getSellerProfile(" ")
        );

        verifyNoInteractions(jwtProvider);
    }

    // GET ALL SELLERS

    @Test
    void getAllSellers_shouldReturnSellersByStatus() {
        AccountStatus status = AccountStatus.values()[0];

        when(sellerRepository.findByAccountStatus(status))
                .thenReturn(List.of(existingSeller));

        List<Seller> result = sellerService.getAllSellers(status);

        assertEquals(1, result.size());
        assertSame(existingSeller, result.get(0));

        verify(sellerRepository).findByAccountStatus(status);
    }

    @Test
    void getAllSellers_shouldRejectNullStatus() {
        assertThrows(
                IllegalArgumentException.class,
                () -> sellerService.getAllSellers(null)
        );

        verifyNoInteractions(sellerRepository);
    }

    // UPDATE SELLER

    @Test
    void updateSeller_shouldUpdateBasicFields() throws Exception {
        when(sellerRepository.findById(1L))
                .thenReturn(Optional.of(existingSeller));

        when(seller.getSellerName()).thenReturn("Updated Seller");
        when(seller.getMobile()).thenReturn("9876543210");
        when(seller.getEmail()).thenReturn(null);
        when(seller.getBusinessDetails()).thenReturn(null);
        when(seller.getBankDetails()).thenReturn(null);
        when(seller.getPickupAddress()).thenReturn(null);
        when(seller.getGSTIN()).thenReturn(null);

        when(sellerRepository.save(existingSeller))
                .thenReturn(existingSeller);

        Seller result = sellerService.updateSeller(1L, seller);

        assertSame(existingSeller, result);

        verify(existingSeller).setSellerName("Updated Seller");
        verify(existingSeller).setMobile("9876543210");
        verify(sellerRepository).save(existingSeller);
    }

    @Test
    void updateSeller_shouldThrowWhenSellerDoesNotExist() {
        when(sellerRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                SellerException.class,
                () -> sellerService.updateSeller(99L, seller)
        );

        verify(sellerRepository, never()).save(any(Seller.class));
    }

    @Test
    void updateSeller_shouldRejectNullSeller() {
        assertThrows(
                IllegalArgumentException.class,
                () -> sellerService.updateSeller(1L, null)
        );

        verifyNoInteractions(sellerRepository);
    }

    @Test
    void updateSeller_shouldRejectNullId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> sellerService.updateSeller(null, seller)
        );

        verifyNoInteractions(sellerRepository);
    }

    // DELETE SELLER

    @Test
    void deleteSeller_shouldDeleteExistingSeller() throws Exception {
        when(sellerRepository.existsById(1L)).thenReturn(true);

        sellerService.deleteSeller(1L);

        verify(sellerRepository).deleteById(1L);
    }

    @Test
    void deleteSeller_shouldThrowWhenSellerDoesNotExist() {
        when(sellerRepository.existsById(99L)).thenReturn(false);

        assertThrows(
                SellerException.class,
                () -> sellerService.deleteSeller(99L)
        );

        verify(sellerRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteSeller_shouldRejectNullId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> sellerService.deleteSeller(null)
        );

        verifyNoInteractions(sellerRepository);
    }

    // VERIFY EMAIL

    @Test
    void verifyEmail_shouldMarkSellerAsVerified() throws Exception {
        when(sellerRepository.findByEmail("seller@example.com"))
                .thenReturn(existingSeller);

        when(sellerRepository.save(existingSeller))
                .thenReturn(existingSeller);

        Seller result = sellerService.verifyEmail(
                "seller@example.com",
                "123456"
        );

        assertSame(existingSeller, result);
        verify(existingSeller).setEmailVerified(true);
        verify(sellerRepository).save(existingSeller);
    }

    @Test
    void verifyEmail_shouldRejectBlankOtp() {
        assertThrows(
                IllegalArgumentException.class,
                () -> sellerService.verifyEmail(
                        "seller@example.com",
                        " "
                )
        );

        verifyNoInteractions(sellerRepository);
    }

    // UPDATE ACCOUNT STATUS

    @Test
    void updateSellerAccountStatus_shouldUpdateStatus() throws Exception {
        AccountStatus status = AccountStatus.values()[0];

        when(sellerRepository.findById(1L))
                .thenReturn(Optional.of(existingSeller));

        when(sellerRepository.save(existingSeller))
                .thenReturn(existingSeller);

        Seller result =
                sellerService.updateSellerAccountStatus(1L, status);

        assertSame(existingSeller, result);
        verify(existingSeller).setAccountStatus(status);
        verify(sellerRepository).save(existingSeller);
    }

    @Test
    void updateSellerAccountStatus_shouldRejectNullStatus() {
        assertThrows(
                IllegalArgumentException.class,
                () -> sellerService.updateSellerAccountStatus(1L, null)
        );

        verifyNoInteractions(sellerRepository);
    }
}

