
package com.gaurav.controller;

import com.gaurav.dto.RevenueChart;
import com.gaurav.exception.SellerException;
import com.gaurav.model.Seller;
import com.gaurav.service.RevenueService;
import com.gaurav.service.SellerService;
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
class RevenueControllerTest {

    @Mock
    private RevenueService revenueService;

    @Mock
    private SellerService sellerService;

    @InjectMocks
    private RevenueController revenueController;

    @Test
    void getRevenueChart_shouldReturnTodayRevenue() throws SellerException {
        String jwt = "Bearer test-token";
        String type = "today";

        Seller seller = mock(Seller.class);
        Long sellerId = 1L;

        RevenueChart chart = mock(RevenueChart.class);
        List<RevenueChart> expectedRevenue = List.of(chart);

        when(sellerService.getSellerProfile(jwt)).thenReturn(seller);
        when(seller.getId()).thenReturn(sellerId);
        when(revenueService.getRevenueChartByType(type, sellerId))
                .thenReturn(expectedRevenue);

        ResponseEntity<List<RevenueChart>> response =
                revenueController.getRevenueChart(type, jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedRevenue, response.getBody());

        verify(sellerService).getSellerProfile(jwt);
        verify(revenueService).getRevenueChartByType(type, sellerId);
    }

    @Test
    void getRevenueChart_shouldReturnRevenueForRequestedType()
            throws SellerException {

        String jwt = "Bearer test-token";
        String type = "monthly";

        Seller seller = mock(Seller.class);
        Long sellerId = 2L;

        List<RevenueChart> expectedRevenue = List.of();

        when(sellerService.getSellerProfile(jwt)).thenReturn(seller);
        when(seller.getId()).thenReturn(sellerId);
        when(revenueService.getRevenueChartByType(type, sellerId))
                .thenReturn(expectedRevenue);

        ResponseEntity<List<RevenueChart>> response =
                revenueController.getRevenueChart(type, jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(revenueService).getRevenueChartByType(type, sellerId);
    }

    @Test
    void getRevenueChart_shouldPropagateSellerException()
            throws SellerException {

        String jwt = "Bearer test-token";
        SellerException exception = mock(SellerException.class);

        when(sellerService.getSellerProfile(jwt)).thenThrow(exception);

        SellerException thrown = assertThrows(
                SellerException.class,
                () -> revenueController.getRevenueChart("today", jwt)
        );

        assertSame(exception, thrown);
        verifyNoInteractions(revenueService);
    }
}