
package com.gaurav.service.impl;

import com.gaurav.exception.ProductException;
import com.gaurav.model.Category;
import com.gaurav.model.Product;
import com.gaurav.model.Seller;
import com.gaurav.repository.CategoryRepository;
import com.gaurav.repository.ProductRepository;
import com.gaurav.request.CreateProductRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Seller seller;
    private CreateProductRequest request;
    private Category category1;
    private Category category2;
    private Category category3;
    private Product product;

    @BeforeEach
    void setUp() {

        seller = mock(Seller.class);
        lenient().when(seller.getId()).thenReturn(1L);

        request = mock(CreateProductRequest.class);

        // Price getters return Integer
        lenient().when(request.getMrpPrice()).thenReturn(1000);
        lenient().when(request.getSellingPrice()).thenReturn(800);

        lenient().when(request.getCategory()).thenReturn("electronics");
        lenient().when(request.getCategory2()).thenReturn("mobiles");
        lenient().when(request.getCategory3()).thenReturn("smartphones");
        lenient().when(request.getTitle()).thenReturn("Test Phone");
        lenient().when(request.getColor()).thenReturn("Black");
        lenient().when(request.getDescription())
                .thenReturn("Test product description");

        category1 = new Category();
        category1.setCategoryId("electronics");
        category1.setLevel(1);
        category1.setName("electronics");

        category2 = new Category();
        category2.setCategoryId("mobiles");
        category2.setLevel(2);
        category2.setName("mobiles");
        category2.setParentCategory(category1);

        category3 = new Category();
        category3.setCategoryId("smartphones");
        category3.setLevel(3);
        category3.setName("smartphones");
        category3.setParentCategory(category2);

        product = new Product();
        product.setId(10L);
        product.setTitle("Test Phone");
        product.setSeller(seller);
        product.setCategory(category3);

        product.setMrpPrice(1000);
        product.setSellingPrice(800);
        product.setDiscountPercent(20);
    }

    @Test
    void calculateDiscountPercentage_shouldReturnCorrectDiscount() {

        int discount =
                ProductServiceImpl.calculateDiscountPercentage(
                        1000.0, 800.0
                );

        assertEquals(20, discount);
    }

    @Test
    void calculateDiscountPercentage_shouldReturnZeroWhenPricesAreEqual() {

        int discount =
                ProductServiceImpl.calculateDiscountPercentage(
                        500.0, 500.0
                );

        assertEquals(0, discount);
    }

    @Test
    void calculateDiscountPercentage_shouldRejectZeroMrp() {

        assertThrows(
                IllegalArgumentException.class,
                () -> ProductServiceImpl.calculateDiscountPercentage(
                        0.0, 100.0
                )
        );
    }

    @Test
    void calculateDiscountPercentage_shouldRejectSellingPriceAboveMrp() {

        assertThrows(
                IllegalArgumentException.class,
                () -> ProductServiceImpl.calculateDiscountPercentage(
                        100.0, 150.0
                )
        );
    }

    @Test
    void createProduct_shouldCreateProductAndMissingCategories()
            throws Exception {

        when(categoryRepository.findByCategoryId("electronics"))
                .thenReturn(null);

        when(categoryRepository.findByCategoryId("mobiles"))
                .thenReturn(null);

        when(categoryRepository.findByCategoryId("smartphones"))
                .thenReturn(null);

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> {
                    Category saved = invocation.getArgument(0);

                    if (saved.getLevel() == 1) {
                        return category1;
                    }

                    if (saved.getLevel() == 2) {
                        return category2;
                    }

                    return category3;
                });

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> {
                    Product saved = invocation.getArgument(0);
                    saved.setId(10L);
                    return saved;
                });

        Product result = productService.createProduct(request, seller);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Test Phone", result.getTitle());
        assertEquals(20, result.getDiscountPercent());
        assertSame(category3, result.getCategory());

        verify(categoryRepository, times(3))
                .save(any(Category.class));

        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_shouldReuseExistingCategories()
            throws Exception {

        when(categoryRepository.findByCategoryId("electronics"))
                .thenReturn(category1);

        when(categoryRepository.findByCategoryId("mobiles"))
                .thenReturn(category2);

        when(categoryRepository.findByCategoryId("smartphones"))
                .thenReturn(category3);

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> {
                    Product saved = invocation.getArgument(0);
                    saved.setId(10L);
                    return saved;
                });

        Product result = productService.createProduct(request, seller);

        assertNotNull(result);
        assertSame(category3, result.getCategory());

        verify(categoryRepository, never()).save(any(Category.class));
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_shouldRejectNullRequest() {

        assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(null, seller)
        );

        verifyNoInteractions(productRepository, categoryRepository);
    }

    @Test
    void createProduct_shouldRejectNullSeller() {

        assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(request, null)
        );

        verifyNoInteractions(productRepository, categoryRepository);
    }

    @Test
    void createProduct_shouldRejectInvalidPrices() {

        when(request.getMrpPrice()).thenReturn(100);
        when(request.getSellingPrice()).thenReturn(150);

        assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(request, seller)
        );

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void findProductById_shouldReturnProduct() throws Exception {

        when(productRepository.findById(10L))
                .thenReturn(Optional.of(product));

        Product result = productService.findProductById(10L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Test Phone", result.getTitle());
    }

    @Test
    void findProductById_shouldThrowWhenProductDoesNotExist() {

        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductException.class,
                () -> productService.findProductById(99L)
        );
    }

    @Test
    void findProductById_shouldRejectNullId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> productService.findProductById(null)
        );

        verifyNoInteractions(productRepository);
    }

    @Test
    void deleteProduct_shouldDeleteExistingProduct() throws Exception {

        when(productRepository.findById(10L))
                .thenReturn(Optional.of(product));

        productService.deleteProduct(10L);

        verify(productRepository).delete(product);
    }

    @Test
    void deleteProduct_shouldNotDeleteWhenProductDoesNotExist() {

        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductException.class,
                () -> productService.deleteProduct(99L)
        );

        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void searchProduct_shouldReturnMatchingProducts() {

        when(productRepository.searchProduct("phone"))
                .thenReturn(List.of(product));

        List<Product> result =
                productService.searchProduct(" phone ");

        assertEquals(1, result.size());
        assertEquals("Test Phone", result.get(0).getTitle());

        verify(productRepository).searchProduct("phone");
    }

    @Test
    void searchProduct_shouldReturnEmptyForBlankQuery() {

        List<Product> result = productService.searchProduct("  ");

        assertTrue(result.isEmpty());

        verifyNoInteractions(productRepository);
    }

    @Test
    void getProductBySellerId_shouldReturnSellerProducts() {

        when(productRepository.findBySellerId(1L))
                .thenReturn(List.of(product));

        List<Product> result =
                productService.getProductBySellerId(1L);

        assertEquals(1, result.size());

        verify(productRepository).findBySellerId(1L);
    }

    @Test
    void getProductBySellerId_shouldRejectNullSellerId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> productService.getProductBySellerId(null)
        );

        verifyNoInteractions(productRepository);
    }

    @Test
    void recentlyAddedProduct_shouldReturnEmptyListForCurrentImplementation() {

        List<Product> result = productService.recentlyAddedProduct();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllProduct_shouldRejectNegativePageNumber() {

        assertThrows(
                IllegalArgumentException.class,
                () -> productService.getAllProduct(
                        null, null, null, null,
                        null, null, null, null, null, -1
                )
        );

        verify(productRepository, never())
                .findAll(
                        org.mockito.ArgumentMatchers
                                .<org.springframework.data.jpa.domain.Specification<Product>>any(),
                        any(Pageable.class)
                );
    }

    @Test
    void getAllProduct_shouldRejectInvalidPriceRange() {

        assertThrows(
                IllegalArgumentException.class,
                () -> productService.getAllProduct(
                        null, null, null, null,
                        1000, 500, null, null, null, 0
                )
        );
    }

    @Test
    void getAllProduct_shouldReturnPageOfProducts() {

        Page<Product> expectedPage = new PageImpl<>(
                List.of(product),
                PageRequest.of(0, 10),
                1
        );

        when(productRepository.findAll(
                org.mockito.ArgumentMatchers
                        .<org.springframework.data.jpa.domain.Specification<Product>>any(),
                any(Pageable.class)
        )).thenReturn(expectedPage);

        Page<Product> result = productService.getAllProduct(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "price_low",
                null,
                0
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Phone", result.getContent().get(0).getTitle());

        verify(productRepository).findAll(
                org.mockito.ArgumentMatchers
                        .<org.springframework.data.jpa.domain.Specification<Product>>any(),
                any(Pageable.class)
        );
    }
}

