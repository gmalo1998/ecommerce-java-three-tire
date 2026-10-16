
package com.gaurav.controller;

import com.gaurav.domain.OrderStatus;
import com.gaurav.exception.OrderException;
import com.gaurav.exception.SellerException;
import com.gaurav.model.Order;
import com.gaurav.model.Seller;
import com.gaurav.response.ApiResponse;
import com.gaurav.service.OrderService;
import com.gaurav.service.SellerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/seller/orders")
@RequiredArgsConstructor
public class SellerOrderController {

    private final OrderService orderService;
    private final SellerService sellerService;

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrdersHandler(
            @RequestHeader("Authorization") String jwt)
            throws SellerException {

        Seller seller = sellerService.getSellerProfile(jwt);

        log.debug("Fetching orders for seller ID: {}", seller.getId());

        List<Order> orders = orderService.getShopsOrders(seller.getId());

        log.debug("Order retrieval completed for seller ID: {}. Result count: {}",
                seller.getId(), orders.size());

        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/{orderId}/status/{orderStatus}")
    public ResponseEntity<Order> updateOrderHandler(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long orderId,
            @PathVariable OrderStatus orderStatus)
            throws OrderException {

        log.info("Order status update requested. Order ID: {}, new status: {}",
                orderId, orderStatus);

        // IMPORTANT:
        // Verify that this order belongs to the seller identified by jwt
        // before updating its status. Enforce this in the service layer
        // or add an ownership-aware service method.

        Order order = orderService.updateOrderStatus(orderId, orderStatus);

        log.info("Order status update completed. Order ID: {}", orderId);

        return new ResponseEntity<>(order, HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/{orderId}/delete")
    public ResponseEntity<ApiResponse> deleteOrderHandler(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String jwt)
            throws OrderException {

        log.info("Order deletion requested. Order ID: {}", orderId);

        // IMPORTANT:
        // Verify seller ownership before deleting the order.

        orderService.deleteOrder(orderId);

        ApiResponse response =
                new ApiResponse("Order Deleted Successfully", true);

        log.info("Order deletion completed. Order ID: {}", orderId);

        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }
}