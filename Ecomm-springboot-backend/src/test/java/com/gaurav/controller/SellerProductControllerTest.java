
package com.gaurav.controller;

import com.gaurav.exception.ProductException;
import com.gaurav.exception.SellerException;
import com.gaurav.model.Product;
import com.gaurav.model.Seller;
import com.gaurav.request.CreateProductRequest;
import com.gaurav.service.ProductService;
import com.gaurav.service.SellerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private SellerService sellerService;

    @InjectMocks
    private SellerProductController sellerProductController;

    @Test
    void getProductBySellerId_shouldReturnSellerProducts()
            throws ProductException, SellerException {

        String jwt = "Bearer test-token";
        Long sellerId = 1L;

        Seller seller = mock(Seller.class);
        List<Product> products = List.of(mock(Product.class));

        when(sellerService.getSellerProfile(jwt)).thenReturn(seller);
        when(seller.getId()).thenReturn(sellerId);
        when(productService.getProductBySellerId(sellerId))
                .thenReturn(products);

        ResponseEntity<List<Product>> response =
                sellerProductController.getProductBySellerId(jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(products, response.getBody());

        verify(sellerService).getSellerProfile(jwt);
        verify(productService).getProductBySellerId(sellerId);
    }

    @Test
    void getProductBySellerId_shouldReturnEmptyListWhenNoProductsExist()
            throws ProductException, SellerException {

        String jwt = "Bearer test-token";
        Long sellerId = 1L;

        Seller seller = mock(Seller.class);

        when(sellerService.getSellerProfile(jwt)).thenReturn(seller);
        when(seller.getId()).thenReturn(sellerId);
        when(productService.getProductBySellerId(sellerId))
                .thenReturn(List.of());

        ResponseEntity<List<Product>> response =
                sellerProductController.getProductBySellerId(jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void createProduct_shouldReturnCreatedProduct()
            throws Exception {

        String jwt = "Bearer test-token";
        Long sellerId = 1L;

        Seller seller = mock(Seller.class);
        CreateProductRequest request = mock(CreateProductRequest.class);
        Product product = mock(Product.class);

        when(sellerService.getSellerProfile(jwt)).thenReturn(seller);
        when(seller.getId()).thenReturn(sellerId);
        when(productService.createProduct(request, seller))
                .thenReturn(product);

        ResponseEntity<Product> response =
                sellerProductController.createProduct(request, jwt);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(product, response.getBody());

        verify(productService).createProduct(request, seller);
    }

    @Test
    void deleteProduct_shouldReturnNoContent()
            throws ProductException, SellerException {

        Long productId = 10L;
        String jwt = "Bearer test-token";

        Seller seller = mock(Seller.class);

        when(sellerService.getSellerProfile(jwt)).thenReturn(seller);
        when(seller.getId()).thenReturn(1L);

        ResponseEntity<Void> response =
                sellerProductController.deleteProduct(productId, jwt);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(productService).deleteProduct(productId);
    }

    @Test
    void updateProduct_shouldReturnUpdatedProduct()
            throws ProductException, SellerException {

        Long productId = 10L;
        String jwt = "Bearer test-token";

        Seller seller = mock(Seller.class);
        Product requestProduct = mock(Product.class);
        Product updatedProduct = mock(Product.class);

        when(sellerService.getSellerProfile(jwt)).thenReturn(seller);
        when(seller.getId()).thenReturn(1L);
        when(productService.updateProduct(productId, requestProduct))
                .thenReturn(updatedProduct);

        ResponseEntity<Product> response =
                sellerProductController.updateProduct(
                        productId, requestProduct, jwt
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(updatedProduct, response.getBody());

        verify(productService).updateProduct(productId, requestProduct);
    }
}