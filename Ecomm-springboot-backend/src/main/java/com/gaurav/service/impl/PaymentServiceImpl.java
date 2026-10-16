
package com.gaurav.service.impl;

import com.gaurav.domain.PaymentOrderStatus;
import com.gaurav.domain.PaymentStatus;
import com.gaurav.model.Order;
import com.gaurav.model.PaymentOrder;
import com.gaurav.model.User;
import com.gaurav.repository.CartRepository;
import com.gaurav.repository.OrderRepository;
import com.gaurav.repository.PaymentOrderRepository;
import com.gaurav.service.PaymentService;

import com.razorpay.Payment;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Value("${stripe.api.key}")
    private String stripeSecretKey;

    @Value("${razorpay.api.key}")
    private String apiKey;

    @Value("${razorpay.api.secret}")
    private String apiSecret;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    private final PaymentOrderRepository paymentOrderRepository;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;

    @Override
    @Transactional
    public PaymentOrder createOrder(User user, Set<Order> orders) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException(
                    "A valid user must be provided"
            );
        }

        if (orders == null || orders.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one order must be provided"
            );
        }

        long amount = orders.stream()
                .mapToLong(Order::getTotalSellingPrice)
                .sum();

        var cart = cartRepository.findByUserId(user.getId());

        if (cart == null) {
            throw new IllegalStateException(
                    "Cart not found for user ID " + user.getId()
            );
        }

        int couponPrice = cart.getCouponPrice();

        if (couponPrice < 0 || couponPrice > amount) {
            throw new IllegalArgumentException(
                    "Invalid coupon discount for payment"
            );
        }

        PaymentOrder paymentOrder = new PaymentOrder();
        paymentOrder.setUser(user);
        paymentOrder.setAmount(amount - couponPrice);
        paymentOrder.setOrders(orders);

        PaymentOrder savedPaymentOrder =
                paymentOrderRepository.save(paymentOrder);

        log.info(
                "Payment order created for user ID {} with {} associated orders",
                user.getId(),
                orders.size()
        );

        return savedPaymentOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentOrder getPaymentOrderById(Long id) throws Exception {
        if (id == null) {
            throw new IllegalArgumentException(
                    "Payment order ID must not be null"
            );
        }

        return paymentOrderRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Payment order not found for ID: {}", id);
                    return new Exception(
                            "Payment order not found with ID " + id
                    );
                });
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentOrder getPaymentOrderByPaymentId(
            String paymentLinkId) throws Exception {

        if (paymentLinkId == null || paymentLinkId.isBlank()) {
            throw new IllegalArgumentException(
                    "Payment link ID must not be empty"
            );
        }

        PaymentOrder paymentOrder =
                paymentOrderRepository.findByPaymentLinkId(paymentLinkId);

        if (paymentOrder == null) {
            log.warn("Payment order not found for the supplied payment link ID");
            throw new Exception("Payment order not found");
        }

        return paymentOrder;
    }

    @Override
    public Boolean ProceedPaymentOrder(
            PaymentOrder paymentOrder,
            String paymentId,
            String paymentLinkId) throws RazorpayException {

        if (paymentOrder == null
                || paymentOrder.getStatus() != PaymentOrderStatus.PENDING) {
            log.warn("Payment verification skipped because the order is not pending");
            return false;
        }

        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException(
                    "Payment ID must not be empty"
            );
        }

        RazorpayClient razorpay = new RazorpayClient(apiKey, apiSecret);
        Payment payment = razorpay.payments.fetch(paymentId);

        Integer receivedAmount = payment.get("amount");
        String status = payment.get("status");

        // Razorpay represents payment amounts in paise.
        long expectedAmountInPaise = paymentOrder.getAmount() * 100L;

        if (receivedAmount == null
                || receivedAmount.longValue() != expectedAmountInPaise) {
            log.warn("Razorpay payment amount did not match the expected amount");
            return false;
        }

        if (!"captured".equals(status)) {
            paymentOrder.setStatus(PaymentOrderStatus.FAILED);
            paymentOrderRepository.save(paymentOrder);

            log.warn("Razorpay payment was not captured");
            return false;
        }

        Set<Order> orders = paymentOrder.getOrders();

        for (Order order : orders) {
            order.setPaymentStatus(PaymentStatus.COMPLETED);
            orderRepository.save(order);
        }

        paymentOrder.setStatus(PaymentOrderStatus.SUCCESS);
        paymentOrderRepository.save(paymentOrder);

        log.info(
                "Payment verified successfully for payment order ID {}",
                paymentOrder.getId()
        );

        return true;
    }

    @Override
    public PaymentLink createRazorpayPaymentLink(
            User user,
            Long amount,
            Long orderId) throws RazorpayException {

        if (user == null || amount == null || orderId == null) {
            throw new IllegalArgumentException(
                    "User, amount, and order ID must be provided"
            );
        }

        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero"
            );
        }

        long amountInPaise = Math.multiplyExact(amount, 100L);

        RazorpayClient razorpay = new RazorpayClient(apiKey, apiSecret);

        JSONObject paymentLinkRequest = new JSONObject();
        paymentLinkRequest.put("amount", amountInPaise);
        paymentLinkRequest.put("currency", "INR");

        JSONObject customer = new JSONObject();
        customer.put("name", user.getFullName());
        customer.put("email", user.getEmail());
        paymentLinkRequest.put("customer", customer);

        JSONObject notify = new JSONObject();
        notify.put("email", true);
        paymentLinkRequest.put("notify", notify);

        paymentLinkRequest.put("reminder_enable", true);
        paymentLinkRequest.put(
                "callback_url",
                frontendUrl + "/payment-success/" + orderId
        );
        paymentLinkRequest.put("callback_method", "get");

        PaymentLink paymentLink =
                razorpay.paymentLink.create(paymentLinkRequest);

        log.info("Razorpay payment link created for order ID {}", orderId);

        return paymentLink;
    }

    @Override
    public String createStripePaymentLink(
            User user,
            Long amount,
            Long orderId) throws StripeException {

        if (user == null || amount == null || orderId == null) {
            throw new IllegalArgumentException(
                    "User, amount, and order ID must be provided"
            );
        }

        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero"
            );
        }

        Stripe.apiKey = stripeSecretKey;

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(
                        SessionCreateParams.PaymentMethodType.CARD
                )
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(
                        frontendUrl + "/payment-success/" + orderId
                )
                .setCancelUrl(
                        frontendUrl + "/payment/cancel"
                )
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData
                                                .builder()
                                                .setCurrency("usd")
                                                .setUnitAmount(
                                                        Math.multiplyExact(
                                                                amount, 100L
                                                        )
                                                )
                                                .setProductData(
                                                        SessionCreateParams
                                                                .LineItem
                                                                .PriceData
                                                                .ProductData
                                                                .builder()
                                                                .setName(
                                                                        "Order #" + orderId
                                                                )
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        Session session = Session.create(params);

        log.info("Stripe checkout session created for order ID {}", orderId);

        return session.getUrl();
    }
}

