
package com.gaurav.controller;

import com.gaurav.exception.ProductException;
import com.gaurav.model.Product;
import com.gaurav.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    @Test
    void getProductById_shouldReturnProduct() throws ProductException {
        Long productId = 1L;
        Product product = new Product();

        when(productService.findProductById(productId))
                .thenReturn(product);

        ResponseEntity<Product> response =
                productController.getProductById(productId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(product, response.getBody());

        verify(productService).findProductById(productId);
    }

    @Test
    void searchProduct_shouldReturnMatchingProducts() {
        String query = "laptop";
        List<Product> products = List.of(new Product());

        when(productService.searchProduct(query))
                .thenReturn(products);

        ResponseEntity<List<Product>> response =
                productController.searchProduct(query);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(products, response.getBody());

        verify(productService).searchProduct(query);
    }

    @Test
    void searchProduct_shouldAllowMissingQuery() {
        List<Product> products = List.of();

        when(productService.searchProduct(null))
                .thenReturn(products);

        ResponseEntity<List<Product>> response =
                productController.searchProduct(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(productService).searchProduct(null);
    }

    @Test
    void getAllProducts_shouldReturnPagedResults() {
        Product product = new Product();
        Page<Product> productPage = new PageImpl<>(List.of(product));

        when(productService.getAllProduct(
                "electronics",
                "Samsung",
                "black",
                "M",
                100,
                1000,
                10,
                "price",
                "in_stock",
                0
        )).thenReturn(productPage);

        ResponseEntity<Page<Product>> response =
                productController.getAllProducts(
                        "electronics",
                        "Samsung",
                        "black",
                        "M",
                        100,
                        1000,
                        10,
                        "price",
                        "in_stock",
                        0
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(productPage, response.getBody());
        assertEquals(1, response.getBody().getTotalElements());

        verify(productService).getAllProduct(
                "electronics",
                "Samsung",
                "black",
                "M",
                100,
                1000,
                10,
                "price",
                "in_stock",
                0
        );
    }

    @Test
    void getAllProducts_shouldAllowOptionalFiltersToBeNull() {
        Page<Product> emptyPage = new PageImpl<>(List.of());

        when(productService.getAllProduct(
                null, null, null, null, null, null, null, null, null, 0
        )).thenReturn(emptyPage);

        ResponseEntity<Page<Product>> response =
                productController.getAllProducts(
                        null, null, null, null, null, null, null, null, null, 0
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(productService).getAllProduct(
                null, null, null, null, null, null, null, null, null, 0
        );
    }
}