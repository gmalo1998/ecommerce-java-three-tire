
package com.gaurav.controller;

import com.gaurav.domain.PaymentMethod;
import com.gaurav.model.Cart;
import com.gaurav.model.Order;
import com.gaurav.model.PaymentOrder;
import com.gaurav.model.Seller;
import com.gaurav.model.SellerReport;
import com.gaurav.model.User;
import com.gaurav.repository.CartRepository;
import com.gaurav.response.ApiResponse;
import com.gaurav.response.PaymentLinkResponse;
import com.gaurav.service.PaymentService;
import com.gaurav.service.SellerReportService;
import com.gaurav.service.SellerService;
import com.gaurav.service.TransactionService;
import com.gaurav.service.UserService;

import com.razorpay.PaymentLink;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final UserService userService;
    private final PaymentService paymentService;
    private final TransactionService transactionService;
    private final SellerReportService sellerReportService;
    private final SellerService sellerService;
    private final CartRepository cartRepository;

    @PostMapping("/api/payment/{paymentMethod}/order/{orderId}")
    public ResponseEntity<PaymentLinkResponse> paymentHandler(
            @PathVariable PaymentMethod paymentMethod,
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String jwt) throws Exception {

        User user = userService.findUserProfileByJwt(jwt);

        log.info(
                "Payment link creation requested. User ID: {}, order ID: {}, method: {}",
                user.getId(),
                orderId,
                paymentMethod
        );

        PaymentOrder order = paymentService.getPaymentOrderById(orderId);
        PaymentLinkResponse response = new PaymentLinkResponse();

        if (paymentMethod == PaymentMethod.RAZORPAY) {
            PaymentLink paymentLink = paymentService.createRazorpayPaymentLink(
                    user,
                    order.getAmount(),
                    order.getId()
            );

            response.setPayment_link_url(paymentLink.get("short_url"));

            log.info("Razorpay payment link created for payment order ID: {}", orderId);

        } else if (paymentMethod == PaymentMethod.STRIPE) {
            String paymentUrl = paymentService.createStripePaymentLink(
                    user,
                    order.getAmount(),
                    order.getId()
            );

            response.setPayment_link_url(paymentUrl);

            log.info("Stripe payment link created for payment order ID: {}", orderId);

        } else {
            log.warn("Unsupported payment method requested: {}", paymentMethod);
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/payment/{paymentId}")
    public ResponseEntity<ApiResponse> paymentSuccessHandler(
            @PathVariable String paymentId,
            @RequestParam String paymentLinkId,
            @RequestHeader("Authorization") String jwt) throws Exception {

        User user = userService.findUserProfileByJwt(jwt);

        log.info(
                "Payment confirmation received. User ID: {}",
                user.getId()
        );

        PaymentOrder paymentOrder =
                paymentService.getPaymentOrderByPaymentId(paymentLinkId);

        /*
         * IMPORTANT:
         * PaymentService must validate the payment provider's response,
         * payment ID, payment-link ID, amount, currency, and payment-order
         * association. It must also ensure this order belongs to this user.
         */

        boolean paymentSuccess = paymentService.ProceedPaymentOrder(
                paymentOrder,
                paymentId,
                paymentLinkId
        );

        ApiResponse response = new ApiResponse();

        if (!paymentSuccess) {
            log.warn(
                    "Payment verification failed for payment order ID: {}",
                    paymentOrder.getId()
            );

            response.setMessage("Payment verification failed");
            response.setStatus(false);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        /*
         * IMPORTANT:
         * PaymentService should make payment confirmation idempotent.
         * Only the first confirmed transition to PAID should trigger these
         * transaction and seller-report updates. Otherwise, repeated requests
         * can duplicate transactions and sales totals.
         */
        for (Order order : paymentOrder.getOrders()) {
            transactionService.createTransaction(order);

            Seller seller = sellerService.getSellerById(order.getSellerId());
            SellerReport report = sellerReportService.getSellerReport(seller);

            report.setTotalOrders(report.getTotalOrders() + 1);
            report.setTotalEarnings(
                    report.getTotalEarnings() + order.getTotalSellingPrice()
            );
            report.setTotalSales(
                    report.getTotalSales() + order.getOrderItems().size()
            );

            sellerReportService.updateSellerReport(report);
        }

        Cart cart = cartRepository.findByUserId(user.getId());

        if (cart != null) {
            cart.setCouponPrice(0);
            cart.setCouponCode(null);
            cartRepository.save(cart);
        }

        response.setMessage("Payment successful");
        response.setStatus(true);

        log.info(
                "Payment confirmed successfully for payment order ID: {}",
                paymentOrder.getId()
        );

        return ResponseEntity.ok(response);
    }
}