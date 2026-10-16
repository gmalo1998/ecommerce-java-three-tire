
package com.gaurav.service.impl;

import com.gaurav.domain.HomeCategorySection;
import com.gaurav.model.Deal;
import com.gaurav.model.Home;
import com.gaurav.model.HomeCategory;
import com.gaurav.repository.DealRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomeServiceImplTest {

    @Mock
    private DealRepository dealRepository;

    @InjectMocks
    private HomeServiceImpl homeService;

    @Test
    void creatHomePageData_whenCategoriesHaveDifferentSections_groupsThemCorrectly() {
        HomeCategory gridCategory = category(HomeCategorySection.GRID);

        HomeCategory shopCategory =
                category(HomeCategorySection.SHOP_BY_CATEGORIES);

        HomeCategory electricCategory =
                category(HomeCategorySection.ELECTRIC_CATEGORIES);

        HomeCategory dealCategory = category(HomeCategorySection.DEALS);

        List<HomeCategory> categories = List.of(
                gridCategory,
                shopCategory,
                electricCategory,
                dealCategory
        );

        when(dealRepository.findAll()).thenReturn(List.of());

        when(dealRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Home result = homeService.creatHomePageData(categories);

        assertNotNull(result);

        assertEquals(List.of(gridCategory), result.getGrid());
        assertEquals(List.of(shopCategory), result.getShopByCategories());
        assertEquals(
                List.of(electricCategory),
                result.getElectricCategories()
        );
        assertEquals(List.of(dealCategory), result.getDealCategories());

        assertEquals(1, result.getDeals().size());
        assertEquals(10, result.getDeals().get(0).getDiscount());
        assertSame(dealCategory, result.getDeals().get(0).getCategory());

        verify(dealRepository, times(1)).findAll();
        verify(dealRepository).saveAll(anyList());
    }

    @Test
    void creatHomePageData_whenDealsAlreadyExist_reusesExistingDeals() {
        HomeCategory gridCategory = category(HomeCategorySection.GRID);

        Deal existingDeal = mock(Deal.class);

        when(dealRepository.findAll())
                .thenReturn(List.of(existingDeal));

        Home result = homeService.creatHomePageData(List.of(gridCategory));

        assertNotNull(result);
        assertEquals(List.of(gridCategory), result.getGrid());

        // Verify that the existing deal is preserved in the result.
        assertEquals(1, result.getDeals().size());
        assertSame(existingDeal, result.getDeals().get(0));

        verify(dealRepository, times(1)).findAll();
        verify(dealRepository, never()).saveAll(anyList());
    }

    @Test
    void creatHomePageData_whenNoCategories_returnsEmptyGroups() {
        when(dealRepository.findAll()).thenReturn(List.of());

        Home result = homeService.creatHomePageData(List.of());

        assertNotNull(result);

        assertTrue(result.getGrid().isEmpty());
        assertTrue(result.getShopByCategories().isEmpty());
        assertTrue(result.getElectricCategories().isEmpty());
        assertTrue(result.getDealCategories().isEmpty());
        assertTrue(result.getDeals().isEmpty());

        verify(dealRepository, times(1)).findAll();
        verify(dealRepository, never()).saveAll(anyList());
    }

    @Test
    void creatHomePageData_whenCategoriesAreNull_throwsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> homeService.creatHomePageData(null)
        );

        verifyNoInteractions(dealRepository);
    }

    private HomeCategory category(HomeCategorySection section) {
        HomeCategory category = mock(HomeCategory.class);
        when(category.getSection()).thenReturn(section);
        return category;
    }
}

