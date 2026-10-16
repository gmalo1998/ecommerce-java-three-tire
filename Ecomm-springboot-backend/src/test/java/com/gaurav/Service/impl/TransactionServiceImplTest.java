
package com.gaurav.service.impl;

import com.gaurav.model.Order;
import com.gaurav.model.Seller;
import com.gaurav.model.Transaction;
import com.gaurav.model.User;
import com.gaurav.repository.SellerRepository;
import com.gaurav.repository.TransactionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private SellerRepository sellerRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Order order;
    private Seller seller;

    @BeforeEach
    void setUp() {
        order = mock(Order.class);
        seller = mock(Seller.class);

        // Keep setup free of stubbings shared by unrelated tests.
    }

    // CREATE TRANSACTION

    @Test
    void createTransaction_shouldSaveTransactionSuccessfully() {
        User user = mock(User.class);

        when(order.getSellerId()).thenReturn(10L);
        when(order.getUser()).thenReturn(user);
       

        when(sellerRepository.findById(10L))
                .thenReturn(Optional.of(seller));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.createTransaction(order);

        assertNotNull(result);

        ArgumentCaptor<Transaction> captor =
                ArgumentCaptor.forClass(Transaction.class);

        verify(transactionRepository).save(captor.capture());

        Transaction savedTransaction = captor.getValue();

        assertSame(order, savedTransaction.getOrder());
        assertSame(seller, savedTransaction.getSeller());
        assertSame(user, savedTransaction.getCustomer());

        verify(sellerRepository).findById(10L);
    }

    @Test
    void createTransaction_shouldRejectNullOrder() {
        assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.createTransaction(null)
        );

        verifyNoInteractions(sellerRepository, transactionRepository);
    }

    @Test
    void createTransaction_shouldRejectOrderWithoutSellerId() {
        when(order.getSellerId()).thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.createTransaction(order)
        );

        verifyNoInteractions(sellerRepository, transactionRepository);
    }

    @Test
    void createTransaction_shouldRejectOrderWithoutCustomer() {
        when(order.getSellerId()).thenReturn(10L);
        when(order.getUser()).thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.createTransaction(order)
        );

        verifyNoInteractions(sellerRepository, transactionRepository);
    }

    @Test
    void createTransaction_shouldThrowWhenSellerDoesNotExist() {
        when(order.getSellerId()).thenReturn(10L);
        when(order.getUser()).thenReturn(mock(User.class));

        when(sellerRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalStateException.class,
                () -> transactionService.createTransaction(order)
        );

        verify(sellerRepository).findById(10L);

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    // GET TRANSACTIONS BY SELLER

    @Test
    void getTransactionBySeller_shouldReturnSellerTransactions() {
        when(seller.getId()).thenReturn(10L);

        List<Transaction> expected = List.of(
                mock(Transaction.class),
                mock(Transaction.class)
        );

        when(transactionRepository.findBySellerId(10L))
                .thenReturn(expected);

        List<Transaction> result =
                transactionService.getTransactionBySeller(seller);

        assertEquals(2, result.size());
        assertSame(expected, result);

        verify(transactionRepository).findBySellerId(10L);
    }

    @Test
    void getTransactionBySeller_shouldReturnEmptyListWhenNoneExist() {
        when(seller.getId()).thenReturn(10L);

        when(transactionRepository.findBySellerId(10L))
                .thenReturn(List.of());

        List<Transaction> result =
                transactionService.getTransactionBySeller(seller);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(transactionRepository).findBySellerId(10L);
    }

    @Test
    void getTransactionBySeller_shouldRejectNullSeller() {
        assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.getTransactionBySeller(null)
        );

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void getTransactionBySeller_shouldRejectSellerWithoutId() {
        Seller invalidSeller = mock(Seller.class);
        when(invalidSeller.getId()).thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.getTransactionBySeller(invalidSeller)
        );

        verifyNoInteractions(transactionRepository);
    }

    // GET ALL TRANSACTIONS

    @Test
    void getAllTransactions_shouldReturnAllTransactions() {
        List<Transaction> expected = List.of(
                mock(Transaction.class),
                mock(Transaction.class),
                mock(Transaction.class)
        );

        when(transactionRepository.findAll()).thenReturn(expected);

        List<Transaction> result =
                transactionService.getAllTransactions();

        assertEquals(3, result.size());
        assertSame(expected, result);

        verify(transactionRepository).findAll();
    }

    @Test
    void getAllTransactions_shouldReturnEmptyListWhenNoneExist() {
        when(transactionRepository.findAll()).thenReturn(List.of());

        List<Transaction> result =
                transactionService.getAllTransactions();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(transactionRepository).findAll();
    }
}

