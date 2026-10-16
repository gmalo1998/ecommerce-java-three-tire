
package com.gaurav.controller;

import com.gaurav.model.Deal;
import com.gaurav.response.ApiResponse;
import com.gaurav.service.DealService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DealControllerTest {

    @Mock
    private DealService dealService;

    @InjectMocks
    private DealController dealController;

    @Test
    void createDeals_shouldReturnCreatedDeal() {
        Deal request = mock(Deal.class);
        Deal createdDeal = mock(Deal.class);

        when(dealService.createDeal(request)).thenReturn(createdDeal);

        ResponseEntity<Deal> response = dealController.createDeals(request);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertSame(createdDeal, response.getBody());

        verify(dealService).createDeal(request);
    }

    @Test
    void getDeals_shouldReturnDealList() {
        List<Deal> deals = List.of(mock(Deal.class), mock(Deal.class));

        when(dealService.getDeals()).thenReturn(deals);

        ResponseEntity<List<Deal>> response = dealController.getDeals();

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertSame(deals, response.getBody());
        assertEquals(2, response.getBody().size());

        verify(dealService).getDeals();
    }

    @Test
    void updateDeal_shouldReturnUpdatedDeal() throws Exception {
        Long dealId = 10L;
        Deal request = mock(Deal.class);
        Deal updatedDeal = mock(Deal.class);

        when(dealService.updateDeal(request, dealId)).thenReturn(updatedDeal);

        ResponseEntity<Deal> response =
                dealController.updateDeal(dealId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(updatedDeal, response.getBody());

        verify(dealService).updateDeal(request, dealId);
    }

    @Test
    void deleteDeals_shouldReturnSuccessResponse() throws Exception {
        Long dealId = 10L;

        ResponseEntity<ApiResponse> response =
                dealController.deleteDeals(dealId);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Deal deleted", response.getBody().getMessage());

        verify(dealService).deleteDeal(dealId);
    }
}