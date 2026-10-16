
package com.gaurav.service.impl;

import com.gaurav.domain.OrderStatus;
import com.gaurav.domain.PaymentStatus;
import com.gaurav.exception.OrderException;
import com.gaurav.model.Address;
import com.gaurav.model.Cart;
import com.gaurav.model.CartItem;
import com.gaurav.model.Order;
import com.gaurav.model.OrderItem;
import com.gaurav.model.User;
import com.gaurav.repository.AddressRepository;
import com.gaurav.repository.OrderItemRepository;
import com.gaurav.repository.OrderRepository;
import com.gaurav.repository.UserRepository;
import com.gaurav.service.CartService;
import com.gaurav.service.OrderItemService;
import com.gaurav.service.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImplementation implements OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final OrderItemService orderItemService;
    private final OrderItemRepository orderItemRepository;

    @Override
    @Transactional
    public Set<Order> createOrder(
            User user,
            Address shippingAddress,
            Cart cart) {

        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException(
                    "Valid user must be provided"
            );
        }

        if (shippingAddress == null) {
            throw new IllegalArgumentException(
                    "Shipping address must be provided"
            );
        }

        if (cart == null || cart.getCartItems() == null
                || cart.getCartItems().isEmpty()) {
            throw new IllegalArgumentException(
                    "Cart must contain at least one item"
            );
        }

        if (user.getAddresses() == null) {
            throw new IllegalStateException(
                    "User address collection is not initialized"
            );
        }

        if (!user.getAddresses().contains(shippingAddress)) {
            user.getAddresses().add(shippingAddress);
        }

        Address savedAddress =
                addressRepository.save(shippingAddress);

        Map<Long, List<CartItem>> itemsBySeller =
                cart.getCartItems().stream()
                        .collect(Collectors.groupingBy(item -> {
                            if (item.getProduct() == null
                                    || item.getProduct().getSeller() == null
                                    || item.getProduct().getSeller().getId() == null) {
                                throw new IllegalArgumentException(
                                        "Every cart item must have a product with a valid seller"
                                );
                            }

                            return item.getProduct()
                                    .getSeller()
                                    .getId();
                        }));

        Set<Order> orders = new HashSet<>();

        for (Map.Entry<Long, List<CartItem>> entry
                : itemsBySeller.entrySet()) {

            Long sellerId = entry.getKey();
            List<CartItem> cartItems = entry.getValue();

            int totalOrderPrice = cartItems.stream()
                    .mapToInt(CartItem::getSellingPrice)
                    .sum();

            int totalItems = cartItems.stream()
                    .mapToInt(CartItem::getQuantity)
                    .sum();

            Order createdOrder = new Order();
            createdOrder.setUser(user);
            createdOrder.setSellerId(sellerId);
            createdOrder.setTotalMrpPrice(totalOrderPrice);
            createdOrder.setTotalSellingPrice(totalOrderPrice);
            createdOrder.setTotalItem(totalItems);
            createdOrder.setShippingAddress(savedAddress);
            createdOrder.setOrderStatus(OrderStatus.PENDING);
            createdOrder.getPaymentDetails()
                    .setStatus(PaymentStatus.PENDING);

            Order savedOrder = orderRepository.save(createdOrder);

            List<OrderItem> createdOrderItems = new ArrayList<>();

            for (CartItem cartItem : cartItems) {
                OrderItem orderItem = new OrderItem();

                orderItem.setOrder(savedOrder);
                orderItem.setMrpPrice(cartItem.getMrpPrice());
                orderItem.setProduct(cartItem.getProduct());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setSize(cartItem.getSize());
                orderItem.setUserId(cartItem.getUserId());
                orderItem.setSellingPrice(cartItem.getSellingPrice());

                OrderItem savedOrderItem =
                        orderItemRepository.save(orderItem);

                createdOrderItems.add(savedOrderItem);
            }

            savedOrder.getOrderItems().addAll(createdOrderItems);
            orders.add(savedOrder);

            log.info(
                    "Order created for seller ID {} with {} items",
                    sellerId,
                    createdOrderItems.size()
            );
        }

        log.info(
                "Order creation completed for user ID {}. Number of seller orders: {}",
                user.getId(),
                orders.size()
        );

        return orders;
    }

    @Override
    @Transactional(readOnly = true)
    public Order findOrderById(Long orderId) throws OrderException {

        if (orderId == null) {
            throw new IllegalArgumentException(
                    "Order ID must not be null"
            );
        }

        return orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Order not found for ID: {}", orderId);
                    return new OrderException(
                            "Order not exist with id " + orderId
                    );
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> usersOrderHistory(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "User ID must not be null"
            );
        }

        List<Order> orders = orderRepository.findByUserId(userId);

        log.debug(
                "Retrieved {} orders for user ID {}",
                orders.size(),
                userId
        );

        return orders;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getShopsOrders(Long sellerId) {
        if (sellerId == null) {
            throw new IllegalArgumentException(
                    "Seller ID must not be null"
            );
        }

        List<Order> orders =
                orderRepository.findBySellerIdOrderByOrderDateDesc(sellerId);

        log.debug(
                "Retrieved {} orders for seller ID {}",
                orders.size(),
                sellerId
        );

        return orders;
    }

    @Override
    @Transactional
    public Order updateOrderStatus(
            Long orderId,
            OrderStatus orderStatus) throws OrderException {

        if (orderStatus == null) {
            throw new IllegalArgumentException(
                    "Order status must not be null"
            );
        }

        Order order = findOrderById(orderId);
        order.setOrderStatus(orderStatus);

        Order updatedOrder = orderRepository.save(order);

        log.info(
                "Order status updated for order ID {} to {}",
                orderId,
                orderStatus
        );

        return updatedOrder;
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId) throws OrderException {
        Order order = findOrderById(orderId);

        orderRepository.delete(order);

        log.info("Order deleted with ID: {}", orderId);
    }

    @Override
    @Transactional
    public Order cancelOrder(
            Long orderId,
            User user) throws OrderException {

        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException(
                    "Valid user must be provided"
            );
        }

        Order order = findOrderById(orderId);

        if (order.getUser() == null
                || !Objects.equals(
                        user.getId(),
                        order.getUser().getId())) {

            log.warn(
                    "User ID {} attempted to cancel an order they do not own",
                    user.getId()
            );

            throw new OrderException(
                    "You cannot perform this action on order " + orderId
            );
        }

        order.setOrderStatus(OrderStatus.CANCELLED);

        Order cancelledOrder = orderRepository.save(order);

        log.info(
                "Order cancelled successfully with ID: {}",
                orderId
        );

        return cancelledOrder;
    }
}

