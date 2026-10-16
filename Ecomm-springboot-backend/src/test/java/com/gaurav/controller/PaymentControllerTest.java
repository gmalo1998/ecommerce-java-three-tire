
package com.gaurav.controller;

import com.gaurav.domain.PaymentMethod;
import com.gaurav.model.PaymentOrder;
import com.gaurav.model.User;
import com.gaurav.repository.CartRepository;
import com.gaurav.response.PaymentLinkResponse;
import com.gaurav.response.ApiResponse;
import com.gaurav.service.PaymentService;
import com.gaurav.service.SellerReportService;
import com.gaurav.service.SellerService;
import com.gaurav.service.TransactionService;
import com.gaurav.service.UserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    private static final String JWT = "test-jwt-token";
    private static final Long USER_ID = 1L;
    private static final Long PAYMENT_ORDER_ID = 10L;

    @Mock
    private UserService userService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private SellerReportService sellerReportService;

    @Mock
    private SellerService sellerService;

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private PaymentController paymentController;

    @Test
    void paymentHandler_stripe_shouldReturnPaymentLink() throws Exception {
        User user = mock(User.class);
        PaymentOrder paymentOrder = mock(PaymentOrder.class);

        when(user.getId()).thenReturn(USER_ID);
        when(userService.findUserProfileByJwt(JWT)).thenReturn(user);
        when(paymentService.getPaymentOrderById(PAYMENT_ORDER_ID))
                .thenReturn(paymentOrder);
        when(paymentOrder.getId()).thenReturn(PAYMENT_ORDER_ID);
        when(paymentService.createStripePaymentLink(
                eq(user),
                any(),
                eq(PAYMENT_ORDER_ID)
        )).thenReturn("https://example.com/test-payment");

        ResponseEntity<PaymentLinkResponse> response =
                paymentController.paymentHandler(
                        PaymentMethod.STRIPE,
                        PAYMENT_ORDER_ID,
                        JWT
                );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(
                "https://example.com/test-payment",
                response.getBody().getPayment_link_url()
        );

        verify(paymentService).createStripePaymentLink(
                eq(user), any(), eq(PAYMENT_ORDER_ID)
        );
    }

    @Test
    void paymentSuccessHandler_whenVerificationFails_shouldReturnBadRequest()
            throws Exception {

        User user = mock(User.class);
        PaymentOrder paymentOrder = mock(PaymentOrder.class);

        when(user.getId()).thenReturn(USER_ID);
        when(userService.findUserProfileByJwt(JWT)).thenReturn(user);
        when(paymentService.getPaymentOrderByPaymentId("test-link-id"))
                .thenReturn(paymentOrder);
        when(paymentService.ProceedPaymentOrder(
                paymentOrder,
                "test-payment-id",
                "test-link-id"
        )).thenReturn(false);

        ResponseEntity<ApiResponse> response =
                paymentController.paymentSuccessHandler(
                        "test-payment-id",
                        "test-link-id",
                        JWT
                );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(
                "Payment verification failed",
                response.getBody().getMessage()
        );
        assertFalse(response.getBody().isStatus());

        verify(transactionService, never()).createTransaction(any());
        verifyNoInteractions(sellerService, sellerReportService);
        verifyNoInteractions(cartRepository);
    }
}