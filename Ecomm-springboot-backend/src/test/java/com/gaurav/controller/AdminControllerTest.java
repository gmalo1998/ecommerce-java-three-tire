
package com.gaurav.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.gaurav.domain.AccountStatus;
import com.gaurav.model.HomeCategory;
import com.gaurav.model.Seller;
import com.gaurav.service.HomeCategoryService;
import com.gaurav.service.SellerService;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private SellerService sellerService;

    @Mock
    private HomeCategoryService homeCategoryService;

    @InjectMocks
    private AdminController adminController;

    private Seller seller;
    private HomeCategory category;

    @BeforeEach
    void setUp() {
        seller = org.mockito.Mockito.mock(Seller.class);
        category = org.mockito.Mockito.mock(HomeCategory.class);
    }

    @Test
    void shouldUpdateSellerStatus() throws Exception {
        Long sellerId = 1L;
        AccountStatus status = AccountStatus.values()[0];

        when(sellerService.updateSellerAccountStatus(sellerId, status))
                .thenReturn(seller);

        ResponseEntity<Seller> response =
                adminController.updateSellerStatus(sellerId, status);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(seller, response.getBody());

        verify(sellerService)
                .updateSellerAccountStatus(sellerId, status);
    }

    @Test
    void shouldReturnAllHomeCategories() throws Exception {
        List<HomeCategory> categories =
                Arrays.asList(category);

        when(homeCategoryService.getAllCategories())
                .thenReturn(categories);

        ResponseEntity<List<HomeCategory>> response =
                adminController.getHomeCategory();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(categories, response.getBody());

        verify(homeCategoryService).getAllCategories();
    }

    @Test
    void shouldUpdateHomeCategory() throws Exception {
        Long categoryId = 10L;

        when(homeCategoryService.updateCategory(category, categoryId))
                .thenReturn(category);

        ResponseEntity<HomeCategory> response =
                adminController.updateHomeCategory(categoryId, category);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(category, response.getBody());

        verify(homeCategoryService)
                .updateCategory(category, categoryId);
    }
}