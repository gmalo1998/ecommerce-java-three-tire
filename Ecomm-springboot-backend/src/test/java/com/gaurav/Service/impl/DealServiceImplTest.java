
package com.gaurav.service.impl;

import com.gaurav.model.Deal;
import com.gaurav.model.HomeCategory;
import com.gaurav.repository.DealRepository;
import com.gaurav.repository.HomeCategoryRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DealServiceImplTest {

    @Mock
    private DealRepository dealRepository;

    @Mock
    private HomeCategoryRepository homeCategoryRepository;

    @InjectMocks
    private DealServiceImpl dealService;

    private HomeCategory category;
    private Deal deal;

    @BeforeEach
    void setUp() {
        category = mock(HomeCategory.class);
        deal = mock(Deal.class);
    }

    @Test
    void createDeal_whenCategoryExists_savesDeal() {
        when(category.getId()).thenReturn(1L);
        when(deal.getCategory()).thenReturn(category);
        when(deal.getDiscount()).thenReturn(20);

        when(homeCategoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(dealRepository.save(any(Deal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Deal result = dealService.createDeal(deal);

        assertNotNull(result);
        assertSame(category, result.getCategory());
        assertEquals(20, result.getDiscount());

        verify(homeCategoryRepository).findById(1L);
        verify(dealRepository).save(any(Deal.class));
    }

    @Test
    void createDeal_whenDealIsNull_throwsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> dealService.createDeal(null)
        );

        verifyNoInteractions(dealRepository, homeCategoryRepository);
    }

    @Test
    void createDeal_whenCategoryIsMissing_throwsException() {
        when(deal.getCategory()).thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> dealService.createDeal(deal)
        );

        verifyNoInteractions(dealRepository, homeCategoryRepository);
    }

    @Test
    void createDeal_whenCategoryDoesNotExist_throwsException() {
        when(category.getId()).thenReturn(1L);
        when(deal.getCategory()).thenReturn(category);

        when(homeCategoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> dealService.createDeal(deal)
        );

        verify(dealRepository, never()).save(any(Deal.class));
    }

    @Test
    void getDeals_returnsAllDeals() {
        List<Deal> expectedDeals = List.of(
                mock(Deal.class),
                mock(Deal.class)
        );

        when(dealRepository.findAll()).thenReturn(expectedDeals);

        List<Deal> result = dealService.getDeals();

        assertEquals(2, result.size());
        assertSame(expectedDeals, result);

        verify(dealRepository).findAll();
    }

    @Test
    void updateDeal_whenDealExists_updatesDiscount() throws Exception {
        Deal existingDeal = mock(Deal.class);

        when(dealRepository.findById(10L))
                .thenReturn(Optional.of(existingDeal));

        when(deal.getDiscount()).thenReturn(30);
        when(deal.getCategory()).thenReturn(null);

        when(dealRepository.save(existingDeal))
                .thenReturn(existingDeal);

        Deal result = dealService.updateDeal(deal, 10L);

        verify(existingDeal).setDiscount(30);
        verify(dealRepository).save(existingDeal);

        assertSame(existingDeal, result);
    }

    @Test
    void updateDeal_whenCategoryExists_updatesCategory() throws Exception {
        Deal existingDeal = mock(Deal.class);

        when(category.getId()).thenReturn(1L);

        when(dealRepository.findById(10L))
                .thenReturn(Optional.of(existingDeal));

        when(deal.getDiscount()).thenReturn(null);
        when(deal.getCategory()).thenReturn(category);

        when(homeCategoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(dealRepository.save(existingDeal))
                .thenReturn(existingDeal);

        Deal result = dealService.updateDeal(deal, 10L);

        verify(existingDeal).setCategory(category);
        verify(dealRepository).save(existingDeal);

        assertSame(existingDeal, result);
    }

    @Test
    void updateDeal_whenDealDoesNotExist_throwsException() {
        when(dealRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                Exception.class,
                () -> dealService.updateDeal(deal, 10L)
        );

        verify(dealRepository, never()).save(any(Deal.class));
    }

    @Test
    void updateDeal_whenCategoryDoesNotExist_throwsException() {
        Deal existingDeal = mock(Deal.class);

        when(category.getId()).thenReturn(1L);

        when(dealRepository.findById(10L))
                .thenReturn(Optional.of(existingDeal));

        when(deal.getDiscount()).thenReturn(null);
        when(deal.getCategory()).thenReturn(category);

        when(homeCategoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> dealService.updateDeal(deal, 10L)
        );

        verify(dealRepository, never()).save(any(Deal.class));
    }

    @Test
    void deleteDeal_whenDealExists_deletesDeal() throws Exception {
        Deal existingDeal = mock(Deal.class);

        when(dealRepository.findById(10L))
                .thenReturn(Optional.of(existingDeal));

        dealService.deleteDeal(10L);

        verify(dealRepository).delete(existingDeal);
    }

    @Test
    void deleteDeal_whenDealDoesNotExist_throwsException() {
        when(dealRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                Exception.class,
                () -> dealService.deleteDeal(10L)
        );

        verify(dealRepository, never()).delete(any(Deal.class));
    }
}

