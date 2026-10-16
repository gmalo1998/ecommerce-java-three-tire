
package com.gaurav.service.impl;

import com.gaurav.exception.OrderException;
import com.gaurav.model.OrderItem;
import com.gaurav.repository.OrderItemRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceImplTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderItemServiceImpl orderItemService;

    @Test
    void getOrderItemById_whenItemExists_returnsOrderItem() throws Exception {
        Long id = 1L;
        OrderItem expectedItem = mock(OrderItem.class);

        when(orderItemRepository.findById(id))
                .thenReturn(Optional.of(expectedItem));

        OrderItem result = orderItemService.getOrderItemById(id);

        assertSame(expectedItem, result);
        verify(orderItemRepository).findById(id);
    }

    @Test
    void getOrderItemById_whenItemDoesNotExist_throwsOrderException() {
        Long id = 99L;

        when(orderItemRepository.findById(id))
                .thenReturn(Optional.empty());

        OrderException exception = assertThrows(
                OrderException.class,
                () -> orderItemService.getOrderItemById(id)
        );

        assertEquals("Order item not found", exception.getMessage());
        verify(orderItemRepository).findById(id);
    }

    @Test
    void getOrderItemById_whenIdIsNull_throwsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> orderItemService.getOrderItemById(null)
        );

        verifyNoInteractions(orderItemRepository);
    }
}

