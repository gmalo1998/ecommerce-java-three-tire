
package com.gaurav.controller;

import com.gaurav.config.JwtProvider;
import com.gaurav.domain.AccountStatus;
import com.gaurav.exception.SellerException;
import com.gaurav.model.Seller;
import com.gaurav.model.SellerReport;
import com.gaurav.repository.VerificationCodeRepository;
import com.gaurav.service.EmailService;
import com.gaurav.service.SellerReportService;
import com.gaurav.service.SellerService;
import com.gaurav.service.VerificationService;
import com.gaurav.service.impl.CustomeUserServiceImplementation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerControllerTest {

    @Mock
    private SellerService sellerService;

    @Mock
    private SellerReportService sellerReportService;

    @Mock
    private EmailService emailService;

    @Mock
    private VerificationCodeRepository verificationCodeRepository;

    @Mock
    private VerificationService verificationService;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private CustomeUserServiceImplementation customeUserServiceImplementation;

    @InjectMocks
    private SellerController sellerController;

    @Test
    void getSellerById_shouldReturnSeller() throws SellerException {
        Long sellerId = 1L;
        Seller seller = mock(Seller.class);

        when(sellerService.getSellerById(sellerId)).thenReturn(seller);

        ResponseEntity<Seller> response =
                sellerController.getSellerById(sellerId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(seller, response.getBody());

        verify(sellerService).getSellerById(sellerId);
    }

    @Test
    void getSellerByJwt_shouldReturnSellerProfile() throws SellerException {
        String jwt = "Bearer test-token";
        String email = "seller@example.com";
        Seller seller = mock(Seller.class);

        when(jwtProvider.getEmailFromJwtToken(jwt)).thenReturn(email);
        when(sellerService.getSellerByEmail(email)).thenReturn(seller);

        ResponseEntity<Seller> response =
                sellerController.getSellerByJwt(jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(seller, response.getBody());

        verify(jwtProvider).getEmailFromJwtToken(jwt);
        verify(sellerService).getSellerByEmail(email);
    }

    @Test
    void getSellerReport_shouldReturnReport() throws SellerException {
        String jwt = "Bearer test-token";
        String email = "seller@example.com";
        Seller seller = mock(Seller.class);
        SellerReport report = mock(SellerReport.class);

        when(jwtProvider.getEmailFromJwtToken(jwt)).thenReturn(email);
        when(sellerService.getSellerByEmail(email)).thenReturn(seller);
        when(sellerReportService.getSellerReport(seller)).thenReturn(report);

        ResponseEntity<SellerReport> response =
                sellerController.getSellerReport(jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(report, response.getBody());

        verify(sellerReportService).getSellerReport(seller);
    }

    @Test
    void getAllSellers_shouldReturnSellerList() {
        List<Seller> sellers = List.of(mock(Seller.class));

        when(sellerService.getAllSellers(null)).thenReturn(sellers);

        ResponseEntity<List<Seller>> response =
                sellerController.getAllSellers(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(sellers, response.getBody());

        verify(sellerService).getAllSellers(null);
    }

    @Test
    void getAllSellers_shouldPassStatusFilterToService() {
        AccountStatus status = AccountStatus.ACTIVE;
        List<Seller> sellers = List.of();

        when(sellerService.getAllSellers(status)).thenReturn(sellers);

        ResponseEntity<List<Seller>> response =
                sellerController.getAllSellers(status);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(sellerService).getAllSellers(status);
    }

    @Test
    void updateSeller_shouldReturnUpdatedProfile() throws SellerException {
        String jwt = "Bearer test-token";
        Long sellerId = 1L;

        Seller existingSeller = mock(Seller.class);
        Seller requestSeller = mock(Seller.class);
        Seller updatedSeller = mock(Seller.class);

        when(sellerService.getSellerProfile(jwt)).thenReturn(existingSeller);
        when(existingSeller.getId()).thenReturn(sellerId);
        when(sellerService.updateSeller(sellerId, requestSeller))
                .thenReturn(updatedSeller);

        ResponseEntity<Seller> response =
                sellerController.updateSeller(jwt, requestSeller);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(updatedSeller, response.getBody());

        verify(sellerService).updateSeller(sellerId, requestSeller);
    }

    @Test
    void deleteSeller_shouldReturnNoContent() throws SellerException {
        Long sellerId = 1L;

        doNothing().when(sellerService).deleteSeller(sellerId);

        ResponseEntity<Void> response =
                sellerController.deleteSeller(sellerId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(sellerService).deleteSeller(sellerId);
    }
}