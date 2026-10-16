
package com.gaurav.controller;

import com.gaurav.exception.OrderException;
import com.gaurav.exception.UserException;
import com.gaurav.model.Order;
import com.gaurav.model.OrderItem;
import com.gaurav.model.User;
import com.gaurav.service.CartService;
import com.gaurav.service.OrderItemService;
import com.gaurav.service.OrderService;
import com.gaurav.service.PaymentService;
import com.gaurav.service.SellerReportService;
import com.gaurav.service.SellerService;
import com.gaurav.service.UserService;
import com.gaurav.repository.PaymentOrderRepository;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private static final String JWT = "test-jwt-token";
    private static final Long USER_ID = 1L;
    private static final Long ORDER_ID = 10L;
    private static final Long ORDER_ITEM_ID = 100L;

    @Mock
    private OrderService orderService;

    @Mock
    private UserService userService;

    @Mock
    private OrderItemService orderItemService;

    @Mock
    private CartService cartService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private PaymentOrderRepository paymentOrderRepository;

    @Mock
    private SellerReportService sellerReportService;

    @Mock
    private SellerService sellerService;

    @InjectMocks
    private OrderController orderController;

    @Test
    void usersOrderHistoryHandler_shouldReturnUserOrders()
            throws UserException {

        User user = mock(User.class);
        List<Order> orders = List.of(mock(Order.class), mock(Order.class));

        when(user.getId()).thenReturn(USER_ID);
        when(userService.findUserProfileByJwt(JWT)).thenReturn(user);
        when(orderService.usersOrderHistory(USER_ID)).thenReturn(orders);

        ResponseEntity<List<Order>> response =
                orderController.usersOrderHistoryHandler(JWT);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertSame(orders, response.getBody());

        verify(orderService).usersOrderHistory(USER_ID);
    }

    @Test
    void getOrderById_shouldReturnOrder()
            throws UserException, OrderException {

        User user = mock(User.class);
        Order order = mock(Order.class);

        when(user.getId()).thenReturn(USER_ID);
        when(userService.findUserProfileByJwt(JWT)).thenReturn(user);
        when(orderService.findOrderById(ORDER_ID)).thenReturn(order);

        ResponseEntity<Order> response =
                orderController.getOrderById(ORDER_ID, JWT);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertSame(order, response.getBody());

        verify(orderService).findOrderById(ORDER_ID);
    }

    @Test
    void getOrderItemById_shouldReturnOrderItem() throws Exception {

        User user = mock(User.class);
        OrderItem orderItem = mock(OrderItem.class);

        when(user.getId()).thenReturn(USER_ID);
        when(userService.findUserProfileByJwt(JWT)).thenReturn(user);
        when(orderItemService.getOrderItemById(ORDER_ITEM_ID))
                .thenReturn(orderItem);

        ResponseEntity<OrderItem> response =
                orderController.getOrderItemById(ORDER_ITEM_ID, JWT);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertSame(orderItem, response.getBody());

        verify(orderItemService).getOrderItemById(ORDER_ITEM_ID);
    }
}