
package com.gaurav.service.impl;

import com.gaurav.model.Order;
import com.gaurav.model.Seller;
import com.gaurav.model.Transaction;
import com.gaurav.repository.SellerRepository;
import com.gaurav.repository.TransactionRepository;
import com.gaurav.service.TransactionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final SellerRepository sellerRepository;

    @Override
    @Transactional
    public Transaction createTransaction(Order order) {

        if (order == null) {
            log.warn("Transaction creation rejected: order is null");
            throw new IllegalArgumentException("Order must not be null");
        }

        if (order.getSellerId() == null) {
            log.warn("Transaction creation rejected: order has no seller ID");
            throw new IllegalArgumentException(
                    "Order must have a valid seller ID"
            );
        }

        if (order.getUser() == null) {
            log.warn("Transaction creation rejected: order has no customer");
            throw new IllegalArgumentException(
                    "Order must have a valid customer"
            );
        }

        Long sellerId = order.getSellerId();

        log.debug("Creating transaction for seller ID: {}", sellerId);

        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> {
                    log.warn(
                            "Transaction creation failed: seller ID {} not found",
                            sellerId
                    );
                    return new IllegalStateException(
                            "Seller not found with ID: " + sellerId
                    );
                });

        Transaction transaction = new Transaction();
        transaction.setCustomer(order.getUser());
        transaction.setOrder(order);
        transaction.setSeller(seller);

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        log.info(
                "Transaction created successfully for seller ID: {}",
                sellerId
        );

        return savedTransaction;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionBySeller(Seller seller) {

        if (seller == null || seller.getId() == null) {
            log.warn(
                    "Cannot retrieve transactions: seller or seller ID is null"
            );
            throw new IllegalArgumentException(
                    "Valid seller is required"
            );
        }

        Long sellerId = seller.getId();

        log.debug(
                "Retrieving transactions for seller ID: {}",
                sellerId
        );

        List<Transaction> transactions =
                transactionRepository.findBySellerId(sellerId);

        log.info(
                "Retrieved {} transactions for seller ID: {}",
                transactions.size(),
                sellerId
        );

        return transactions;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactions() {

        log.debug("Retrieving all transactions");

        List<Transaction> transactions =
                transactionRepository.findAll();

        log.info("Retrieved {} transactions", transactions.size());

        return transactions;
    }
}

