
package com.gaurav.service.impl;

import com.gaurav.domain.PaymentOrderStatus;
import com.gaurav.model.Cart;
import com.gaurav.model.Order;
import com.gaurav.model.PaymentOrder;
import com.gaurav.model.User;
import com.gaurav.repository.CartRepository;
import com.gaurav.repository.OrderRepository;
import com.gaurav.repository.PaymentOrderRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentOrderRepository paymentOrderRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void createOrder_whenValidInput_savesPaymentOrder() {
        User user = mock(User.class);
        Order order = mock(Order.class);
        Cart cart = mock(Cart.class);

        when(user.getId()).thenReturn(1L);
        when(order.getTotalSellingPrice()).thenReturn(1000);
        when(cartRepository.findByUserId(1L)).thenReturn(cart);
        when(cart.getCouponPrice()).thenReturn(100);

        when(paymentOrderRepository.save(any(PaymentOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentOrder result =
                paymentService.createOrder(user, Set.of(order));

        assertEquals(900L, result.getAmount());
        assertSame(user, result.getUser());
        assertEquals(Set.of(order), result.getOrders());

        verify(paymentOrderRepository).save(any(PaymentOrder.class));
    }

    @Test
    void createOrder_whenUserIsNull_throwsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.createOrder(null, Set.of(mock(Order.class)))
        );

        verifyNoInteractions(cartRepository, paymentOrderRepository);
    }

    @Test
    void createOrder_whenOrdersAreEmpty_throwsException() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.createOrder(user, Set.of())
        );

        verifyNoInteractions(cartRepository, paymentOrderRepository);
    }

    @Test
    void getPaymentOrderById_whenFound_returnsPaymentOrder() throws Exception {
        PaymentOrder expected = mock(PaymentOrder.class);

        when(paymentOrderRepository.findById(1L))
                .thenReturn(Optional.of(expected));

        PaymentOrder result = paymentService.getPaymentOrderById(1L);

        assertSame(expected, result);
    }

    @Test
    void getPaymentOrderById_whenMissing_throwsException() {
        when(paymentOrderRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                Exception.class,
                () -> paymentService.getPaymentOrderById(99L)
        );
    }

    @Test
    void getPaymentOrderByPaymentId_whenFound_returnsPaymentOrder()
            throws Exception {
        PaymentOrder expected = mock(PaymentOrder.class);

        when(paymentOrderRepository.findByPaymentLinkId("plink_test"))
                .thenReturn(expected);

        PaymentOrder result =
                paymentService.getPaymentOrderByPaymentId("plink_test");

        assertSame(expected, result);
    }

    @Test
    void getPaymentOrderByPaymentId_whenMissing_throwsException() {
        when(paymentOrderRepository.findByPaymentLinkId("missing"))
                .thenReturn(null);

        assertThrows(
                Exception.class,
                () -> paymentService.getPaymentOrderByPaymentId("missing")
        );
    }

    @Test
    void proceedPaymentOrder_whenPaymentOrderIsNotPending_returnsFalse()
            throws Exception {
        PaymentOrder paymentOrder = mock(PaymentOrder.class);

        when(paymentOrder.getStatus())
                .thenReturn(PaymentOrderStatus.SUCCESS);

        ReflectionTestUtils.setField(
                paymentService, "apiKey", "test_key"
        );
        ReflectionTestUtils.setField(
                paymentService, "apiSecret", "test_secret"
        );

        assertFalse(
                paymentService.ProceedPaymentOrder(
                        paymentOrder, "pay_test", "plink_test"
                )
        );

        verifyNoInteractions(orderRepository, paymentOrderRepository);
    }
}

