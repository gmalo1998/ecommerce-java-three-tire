
package com.gaurav.controller;

import com.gaurav.model.Home;
import com.gaurav.model.HomeCategory;
import com.gaurav.service.HomeCategoryService;
import com.gaurav.service.HomeService;

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
class CustomerControllerTest {

    @Mock
    private HomeCategoryService homeCategoryService;

    @Mock
    private HomeService homeService;

    @InjectMocks
    private CustomerController customerController;

    @Test
    void getHomePageData_shouldReturnHomePage() {
        Home home = mock(Home.class);

        when(homeService.getHomePageData()).thenReturn(home);

        ResponseEntity<Home> response =
                customerController.getHomePageData();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(home, response.getBody());

        verify(homeService).getHomePageData();
    }

    @Test
    void createHomeCategories_shouldReturnCreatedHomePage() {
        HomeCategory category = mock(HomeCategory.class);
        List<HomeCategory> requestedCategories = List.of(category);

        Home home = mock(Home.class);

        when(homeCategoryService.createCategories(requestedCategories))
                .thenReturn(requestedCategories);

        when(homeService.creatHomePageData(requestedCategories))
                .thenReturn(home);

        ResponseEntity<Home> response =
                customerController.createHomeCategories(requestedCategories);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(home, response.getBody());

        verify(homeCategoryService).createCategories(requestedCategories);
        verify(homeService).creatHomePageData(requestedCategories);
    }

    @Test
    void createHomeCategories_shouldRejectEmptyList() {
        List<HomeCategory> emptyCategories = List.of();

        ResponseEntity<Home> response =
                customerController.createHomeCategories(emptyCategories);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verifyNoInteractions(homeCategoryService, homeService);
    }
}