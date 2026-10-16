
package com.gaurav.service.impl;

import com.gaurav.model.Seller;
import com.gaurav.model.SellerReport;
import com.gaurav.repository.SellerReportRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerReportServiceImplTest {

    @Mock
    private SellerReportRepository sellerReportRepository;

    @InjectMocks
    private SellerReportServiceImpl sellerReportService;

    private Seller seller;
    private SellerReport sellerReport;

    @BeforeEach
    void setUp() {
        seller = mock(Seller.class);

        // Do not stub seller.getId() here.
        // Only stub it in tests that actually need the ID.

        sellerReport = new SellerReport();
        sellerReport.setSeller(seller);
    }

    @Test
    void getSellerReport_shouldReturnExistingReport() {
        when(seller.getId()).thenReturn(1L);

        when(sellerReportRepository.findBySellerId(1L))
                .thenReturn(sellerReport);

        SellerReport result =
                sellerReportService.getSellerReport(seller);

        assertSame(sellerReport, result);

        verify(sellerReportRepository).findBySellerId(1L);
        verify(sellerReportRepository, never())
                .save(any(SellerReport.class));
    }

    @Test
    void getSellerReport_shouldCreateReportWhenNoneExists() {
        when(seller.getId()).thenReturn(1L);

        when(sellerReportRepository.findBySellerId(1L))
                .thenReturn(null);

        when(sellerReportRepository.save(any(SellerReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SellerReport result =
                sellerReportService.getSellerReport(seller);

        assertNotNull(result);
        assertSame(seller, result.getSeller());

        verify(sellerReportRepository).findBySellerId(1L);
        verify(sellerReportRepository).save(any(SellerReport.class));
    }

    @Test
    void getSellerReport_shouldRejectNullSeller() {
        assertThrows(
                IllegalArgumentException.class,
                () -> sellerReportService.getSellerReport(null)
        );

        verifyNoInteractions(sellerReportRepository);
    }

    @Test
    void getSellerReport_shouldRejectSellerWithoutId() {
        Seller invalidSeller = mock(Seller.class);
        when(invalidSeller.getId()).thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> sellerReportService.getSellerReport(invalidSeller)
        );

        verifyNoInteractions(sellerReportRepository);
    }

    @Test
    void updateSellerReport_shouldSaveAndReturnReport() {
        when(seller.getId()).thenReturn(1L);

        when(sellerReportRepository.save(sellerReport))
                .thenReturn(sellerReport);

        SellerReport result =
                sellerReportService.updateSellerReport(sellerReport);

        assertSame(sellerReport, result);

        verify(sellerReportRepository).save(sellerReport);
    }

    @Test
    void updateSellerReport_shouldRejectNullReport() {
        assertThrows(
                IllegalArgumentException.class,
                () -> sellerReportService.updateSellerReport(null)
        );

        verify(sellerReportRepository, never())
                .save(any(SellerReport.class));
    }

    @Test
    void updateSellerReport_shouldRejectReportWithoutSeller() {
        SellerReport invalidReport = new SellerReport();

        assertThrows(
                IllegalArgumentException.class,
                () -> sellerReportService.updateSellerReport(invalidReport)
        );

        verify(sellerReportRepository, never())
                .save(any(SellerReport.class));
    }

    @Test
    void updateSellerReport_shouldRejectSellerWithoutId() {
        Seller invalidSeller = mock(Seller.class);
        when(invalidSeller.getId()).thenReturn(null);

        SellerReport invalidReport = new SellerReport();
        invalidReport.setSeller(invalidSeller);

        assertThrows(
                IllegalArgumentException.class,
                () -> sellerReportService.updateSellerReport(invalidReport)
        );

        verify(sellerReportRepository, never())
                .save(any(SellerReport.class));
    }
}

