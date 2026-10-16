
package com.gaurav.controller;

import com.gaurav.exception.SellerException;
import com.gaurav.model.Order;
import com.gaurav.model.Seller;
import com.gaurav.model.Transaction;
import com.gaurav.service.SellerService;
import com.gaurav.service.TransactionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;
    private final SellerService sellerService;

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(
            @RequestBody Order order) {

        log.info("Received request to create a transaction");

        Transaction transaction =
                transactionService.createTransaction(order);

        log.info("Transaction created successfully");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transaction);
    }

    @GetMapping("/seller")
    public ResponseEntity<List<Transaction>> getTransactionBySeller(
            @RequestHeader("Authorization") String jwt)
            throws SellerException {

        log.info("Received request to fetch transactions for authenticated seller");

        Seller seller = sellerService.getSellerProfile(jwt);

        List<Transaction> transactions =
                transactionService.getTransactionBySeller(seller);

        log.info("Successfully fetched {} transactions for seller ID {}",
                transactions.size(), seller.getId());

        return ResponseEntity.ok(transactions);
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {

        log.info("Received request to fetch all transactions");

        List<Transaction> transactions =
                transactionService.getAllTransactions();

        log.info("Successfully fetched {} transactions",
                transactions.size());

        return ResponseEntity.ok(transactions);
    }
}

