
package com.gaurav.controller;

import com.gaurav.domain.PaymentMethod;
import com.gaurav.exception.OrderException;
import com.gaurav.exception.SellerException;
import com.gaurav.exception.UserException;
import com.gaurav.model.Address;
import com.gaurav.model.Cart;
import com.gaurav.model.Order;
import com.gaurav.model.OrderItem;
import com.gaurav.model.PaymentOrder;
import com.gaurav.model.Seller;
import com.gaurav.model.SellerReport;
import com.gaurav.model.User;
import com.gaurav.repository.PaymentOrderRepository;
import com.gaurav.response.PaymentLinkResponse;
import com.gaurav.service.CartService;
import com.gaurav.service.OrderItemService;
import com.gaurav.service.OrderService;
import com.gaurav.service.PaymentService;
import com.gaurav.service.SellerReportService;
import com.gaurav.service.SellerService;
import com.gaurav.service.UserService;

import com.razorpay.PaymentLink;
import com.razorpay.RazorpayException;
import com.stripe.exception.StripeException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final OrderItemService orderItemService;
    private final CartService cartService;
    private final PaymentService paymentService;
    private final PaymentOrderRepository paymentOrderRepository;
    private final SellerReportService sellerReportService;
    private final SellerService sellerService;

    @PostMapping
    public ResponseEntity<PaymentLinkResponse> createOrderHandler(
            @RequestBody Address shippingAddress,
            @RequestParam PaymentMethod paymentMethod,
            @RequestHeader("Authorization") String jwt)
            throws UserException, RazorpayException, StripeException {

        log.info("Order creation request received. Payment method: {}", paymentMethod);

        User user = userService.findUserProfileByJwt(jwt);
        Cart cart = cartService.findUserCart(user);

        Set<Order> orders = orderService.createOrder(
                user,
                shippingAddress,
                cart
        );

        PaymentOrder paymentOrder = paymentService.createOrder(user, orders);

        PaymentLinkResponse response = new PaymentLinkResponse();

        if (paymentMethod == PaymentMethod.RAZORPAY) {

            PaymentLink paymentLink = paymentService.createRazorpayPaymentLink(
                    user,
                    paymentOrder.getAmount(),
                    paymentOrder.getId()
            );

            String paymentUrl = paymentLink.get("short_url");
            String paymentUrlId = paymentLink.get("id");

            response.setPayment_link_url(paymentUrl);

            paymentOrder.setPaymentLinkId(paymentUrlId);
            paymentOrderRepository.save(paymentOrder);

            log.info(
                    "Razorpay payment link created for payment order ID: {}",
                    paymentOrder.getId()
            );

        } else {

            String paymentUrl = paymentService.createStripePaymentLink(
                    user,
                    paymentOrder.getAmount(),
                    paymentOrder.getId()
            );

            response.setPayment_link_url(paymentUrl);

            log.info(
                    "Stripe payment link created for payment order ID: {}",
                    paymentOrder.getId()
            );
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user")
    public ResponseEntity<List<Order>> usersOrderHistoryHandler(
            @RequestHeader("Authorization") String jwt)
            throws UserException {

        User user = userService.findUserProfileByJwt(jwt);

        List<Order> orders = orderService.usersOrderHistory(user.getId());

        log.debug(
                "Order history retrieved for user ID: {}. Order count: {}",
                user.getId(),
                orders.size()
        );

        return new ResponseEntity<>(orders, HttpStatus.ACCEPTED);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String jwt)
            throws OrderException, UserException {

        User user = userService.findUserProfileByJwt(jwt);

        Order order = orderService.findOrderById(orderId);

        log.debug(
                "Order lookup requested by user ID: {} for order ID: {}",
                user.getId(),
                orderId
        );

        // IMPORTANT: Verify that this order belongs to the authenticated user
        // before returning it. Enforce this in the service layer if not already done.

        return new ResponseEntity<>(order, HttpStatus.ACCEPTED);
    }

    @GetMapping("/item/{orderItemId}")
    public ResponseEntity<OrderItem> getOrderItemById(
            @PathVariable Long orderItemId,
            @RequestHeader("Authorization") String jwt)
            throws Exception {

        User user = userService.findUserProfileByJwt(jwt);

        OrderItem orderItem = orderItemService.getOrderItemById(orderItemId);

        log.debug(
                "Order item lookup requested by user ID: {} for item ID: {}",
                user.getId(),
                orderItemId
        );

        // IMPORTANT: Verify ownership through the item's associated order
        // before returning it. Enforce this in the service layer if not already done.

        return new ResponseEntity<>(orderItem, HttpStatus.ACCEPTED);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String jwt)
            throws UserException, OrderException, SellerException {

        User user = userService.findUserProfileByJwt(jwt);

        log.info(
                "Order cancellation requested by user ID: {} for order ID: {}",
                user.getId(),
                orderId
        );

        Order order = orderService.cancelOrder(orderId, user);

        Seller seller = sellerService.getSellerById(order.getSellerId());
        SellerReport report = sellerReportService.getSellerReport(seller);

        report.setCanceledOrders(report.getCanceledOrders() + 1);
        report.setTotalRefunds(
                report.getTotalRefunds() + order.getTotalSellingPrice()
        );

        sellerReportService.updateSellerReport(report);

        log.info("Order cancellation completed for order ID: {}", orderId);

        return ResponseEntity.ok(order);
    }
}