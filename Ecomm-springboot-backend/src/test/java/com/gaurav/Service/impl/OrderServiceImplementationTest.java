
package com.gaurav.service.impl;

import com.gaurav.domain.OrderStatus;
import com.gaurav.exception.OrderException;
import com.gaurav.model.Order;
import com.gaurav.model.User;
import com.gaurav.repository.AddressRepository;
import com.gaurav.repository.OrderItemRepository;
import com.gaurav.repository.OrderRepository;
import com.gaurav.repository.UserRepository;
import com.gaurav.service.CartService;
import com.gaurav.service.OrderItemService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplementationTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartService cartService;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderItemService orderItemService;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderServiceImplementation orderService;

    @Test
    void findOrderById_whenOrderExists_returnsOrder() throws Exception {
        Long orderId = 1L;
        Order expectedOrder = mock(Order.class);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(expectedOrder));

        Order result = orderService.findOrderById(orderId);

        assertSame(expectedOrder, result);
        verify(orderRepository).findById(orderId);
    }

    @Test
    void findOrderById_whenOrderDoesNotExist_throwsOrderException() {
        Long orderId = 99L;

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.empty());

        assertThrows(
                OrderException.class,
                () -> orderService.findOrderById(orderId)
        );
    }

    @Test
    void findOrderById_whenIdIsNull_throwsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.findOrderById(null)
        );

        verifyNoInteractions(orderRepository);
    }

    @Test
    void usersOrderHistory_returnsOrdersForUser() {
        Long userId = 1L;
        List<Order> expectedOrders = List.of(
                mock(Order.class),
                mock(Order.class)
        );

        when(orderRepository.findByUserId(userId))
                .thenReturn(expectedOrders);

        List<Order> result = orderService.usersOrderHistory(userId);

        assertSame(expectedOrders, result);
        verify(orderRepository).findByUserId(userId);
    }

    @Test
    void getShopsOrders_returnsOrdersForSeller() {
        Long sellerId = 5L;
        List<Order> expectedOrders = List.of(mock(Order.class));

        when(orderRepository.findBySellerIdOrderByOrderDateDesc(sellerId))
                .thenReturn(expectedOrders);

        List<Order> result = orderService.getShopsOrders(sellerId);

        assertSame(expectedOrders, result);
        verify(orderRepository)
                .findBySellerIdOrderByOrderDateDesc(sellerId);
    }

    @Test
    void updateOrderStatus_whenOrderExists_updatesAndSavesStatus()
            throws Exception {
        Long orderId = 1L;
        Order existingOrder = mock(Order.class);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(existingOrder))
                .thenReturn(existingOrder);

        Order result = orderService.updateOrderStatus(
                orderId,
                OrderStatus.CANCELLED
        );

        verify(existingOrder).setOrderStatus(OrderStatus.CANCELLED);
        verify(orderRepository).save(existingOrder);
        assertSame(existingOrder, result);
    }

    @Test
    void updateOrderStatus_whenStatusIsNull_throwsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.updateOrderStatus(1L, null)
        );
    }

    @Test
    void deleteOrder_whenOrderExists_deletesOrder() throws Exception {
        Long orderId = 1L;
        Order existingOrder = mock(Order.class);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(existingOrder));

        orderService.deleteOrder(orderId);

        verify(orderRepository).delete(existingOrder);
    }

    @Test
    void cancelOrder_whenUserOwnsOrder_cancelsOrder() throws Exception {
        Long orderId = 1L;

        User user = mock(User.class);
        User orderOwner = mock(User.class);
        Order existingOrder = mock(Order.class);

        when(user.getId()).thenReturn(10L);
        when(orderOwner.getId()).thenReturn(10L);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(existingOrder));
        when(existingOrder.getUser()).thenReturn(orderOwner);
        when(orderRepository.save(existingOrder))
                .thenReturn(existingOrder);

        Order result = orderService.cancelOrder(orderId, user);

        verify(existingOrder).setOrderStatus(OrderStatus.CANCELLED);
        verify(orderRepository).save(existingOrder);
        assertSame(existingOrder, result);
    }

    @Test
    void cancelOrder_whenUserDoesNotOwnOrder_throwsOrderException() {
        Long orderId = 1L;

        User user = mock(User.class);
        User orderOwner = mock(User.class);
        Order existingOrder = mock(Order.class);

        when(user.getId()).thenReturn(10L);
        when(orderOwner.getId()).thenReturn(20L);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(existingOrder));
        when(existingOrder.getUser()).thenReturn(orderOwner);

        assertThrows(
                OrderException.class,
                () -> orderService.cancelOrder(orderId, user)
        );

        verify(orderRepository, never()).save(any(Order.class));
    }
}

