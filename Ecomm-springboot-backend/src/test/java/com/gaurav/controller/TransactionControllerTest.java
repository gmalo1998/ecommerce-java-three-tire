
package com.gaurav.controller;

import com.gaurav.exception.SellerException;
import com.gaurav.model.Order;
import com.gaurav.model.Seller;
import com.gaurav.model.Transaction;
import com.gaurav.service.SellerService;
import com.gaurav.service.TransactionService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private SellerService sellerService;

    @InjectMocks
    private TransactionController transactionController;

    @Test
    void createTransactionShouldReturnCreatedTransaction() {
        Order order = mock(Order.class);
        Transaction transaction = mock(Transaction.class);

        when(transactionService.createTransaction(order))
                .thenReturn(transaction);

        ResponseEntity<Transaction> response =
                transactionController.createTransaction(order);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(transaction, response.getBody());

        verify(transactionService).createTransaction(order);
    }

    @Test
    void getTransactionBySellerShouldReturnSellerTransactions()
            throws SellerException {

        String jwt = "Bearer test-token";
        Seller seller = mock(Seller.class);

        List<Transaction> transactions =
                List.of(mock(Transaction.class), mock(Transaction.class));

        when(sellerService.getSellerProfile(jwt)).thenReturn(seller);
        when(transactionService.getTransactionBySeller(seller))
                .thenReturn(transactions);

        ResponseEntity<List<Transaction>> response =
                transactionController.getTransactionBySeller(jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(transactions, response.getBody());

        verify(sellerService).getSellerProfile(jwt);
        verify(transactionService).getTransactionBySeller(seller);
    }

    @Test
    void getAllTransactionsShouldReturnAllTransactions() {
        List<Transaction> transactions =
                List.of(mock(Transaction.class), mock(Transaction.class));

        when(transactionService.getAllTransactions())
                .thenReturn(transactions);

        ResponseEntity<List<Transaction>> response =
                transactionController.getAllTransactions();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(transactions, response.getBody());

        verify(transactionService).getAllTransactions();
    }

    @Test
    void getAllTransactionsShouldReturnEmptyListWhenNoTransactionsExist() {
        when(transactionService.getAllTransactions())
                .thenReturn(List.of());

        ResponseEntity<List<Transaction>> response =
                transactionController.getAllTransactions();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(), response.getBody());

        verify(transactionService).getAllTransactions();
    }
}

