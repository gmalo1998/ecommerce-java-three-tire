
package com.gaurav.controller;

import com.gaurav.domain.OrderStatus;
import com.gaurav.exception.OrderException;
import com.gaurav.exception.SellerException;
import com.gaurav.model.Order;
import com.gaurav.model.Seller;
import com.gaurav.response.ApiResponse;
import com.gaurav.service.OrderService;
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
class SellerOrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private SellerService sellerService;

    @InjectMocks
    private SellerOrderController sellerOrderController;

    @Test
    void getAllOrdersHandler_shouldReturnSellerOrders()
            throws SellerException {

        String jwt = "Bearer test-token";
        Long sellerId = 1L;

        Seller seller = mock(Seller.class);
        List<Order> orders = List.of(mock(Order.class));

        when(sellerService.getSellerProfile(jwt)).thenReturn(seller);
        when(seller.getId()).thenReturn(sellerId);
        when(orderService.getShopsOrders(sellerId)).thenReturn(orders);

        ResponseEntity<List<Order>> response =
                sellerOrderController.getAllOrdersHandler(jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(orders, response.getBody());

        verify(sellerService).getSellerProfile(jwt);
        verify(orderService).getShopsOrders(sellerId);
    }

    @Test
    void getAllOrdersHandler_shouldReturnEmptyListWhenNoOrdersExist()
            throws SellerException {

        String jwt = "Bearer test-token";
        Long sellerId = 1L;

        Seller seller = mock(Seller.class);

        when(sellerService.getSellerProfile(jwt)).thenReturn(seller);
        when(seller.getId()).thenReturn(sellerId);
        when(orderService.getShopsOrders(sellerId)).thenReturn(List.of());

        ResponseEntity<List<Order>> response =
                sellerOrderController.getAllOrdersHandler(jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(orderService).getShopsOrders(sellerId);
    }

    @Test
    void updateOrderHandler_shouldReturnUpdatedOrder()
            throws OrderException {

        String jwt = "Bearer test-token";
        Long orderId = 10L;
        OrderStatus status = OrderStatus.values()[0];
        Order order = mock(Order.class);

        when(orderService.updateOrderStatus(orderId, status))
                .thenReturn(order);

        ResponseEntity<Order> response =
                sellerOrderController.updateOrderHandler(
                        jwt, orderId, status
                );

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertSame(order, response.getBody());

        verify(orderService).updateOrderStatus(orderId, status);
    }

    @Test
    void deleteOrderHandler_shouldReturnSuccessResponse()
            throws OrderException {

        String jwt = "Bearer test-token";
        Long orderId = 10L;

        ResponseEntity<ApiResponse> response =
                sellerOrderController.deleteOrderHandler(orderId, jwt);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(
                "Order Deleted Successfully",
                response.getBody().getMessage()
        );
        assertTrue(response.getBody().isStatus());

        verify(orderService).deleteOrder(orderId);
    }
}