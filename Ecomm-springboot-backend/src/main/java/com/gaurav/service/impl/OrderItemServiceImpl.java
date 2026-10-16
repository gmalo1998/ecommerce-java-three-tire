
package com.gaurav.service.impl;

import com.gaurav.exception.OrderException;
import com.gaurav.model.OrderItem;
import com.gaurav.repository.OrderItemRepository;
import com.gaurav.service.OrderItemService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;

    @Override
    @Transactional(readOnly = true)
    public OrderItem getOrderItemById(Long id) throws Exception {

        if (id == null) {
            throw new IllegalArgumentException(
                    "Order item ID must not be null"
            );
        }

        OrderItem orderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Order item not found for ID: {}", id);
                    return new OrderException("Order item not found");
                });

        log.debug("Order item retrieved successfully for ID: {}", id);

        return orderItem;
    }
}

