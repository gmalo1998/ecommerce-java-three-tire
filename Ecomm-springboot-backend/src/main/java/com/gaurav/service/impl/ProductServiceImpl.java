
package com.gaurav.service.impl;

import com.gaurav.exception.ProductException;
import com.gaurav.model.Category;
import com.gaurav.model.Product;
import com.gaurav.model.Seller;
import com.gaurav.repository.CategoryRepository;
import com.gaurav.repository.ProductRepository;
import com.gaurav.request.CreateProductRequest;
import com.gaurav.service.ProductService;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // CREATE PRODUCT

    @Override
    @Transactional
    public Product createProduct(
            CreateProductRequest req,
            Seller seller
    ) throws ProductException {

        if (req == null) {
            throw new IllegalArgumentException(
                    "Product request must not be null"
            );
        }

        if (seller == null || seller.getId() == null) {
            throw new IllegalArgumentException(
                    "A valid seller is required"
            );
        }

        validatePrices(
                req.getMrpPrice(),
                req.getSellingPrice()
        );

        if (isBlank(req.getCategory())
                || isBlank(req.getCategory2())
                || isBlank(req.getCategory3())) {

            throw new IllegalArgumentException(
                    "All three category levels are required"
            );
        }

        int discountPercentage = calculateDiscountPercentage(
                req.getMrpPrice(),
                req.getSellingPrice()
        );

        Category category1 = getOrCreateCategory(
                req.getCategory(),
                1,
                null
        );

        Category category2 = getOrCreateCategory(
                req.getCategory2(),
                2,
                category1
        );

        Category category3 = getOrCreateCategory(
                req.getCategory3(),
                3,
                category2
        );

        Product product = new Product();

        product.setSeller(seller);
        product.setCategory(category3);
        product.setTitle(req.getTitle());
        product.setColor(req.getColor());
        product.setDescription(req.getDescription());
        product.setDiscountPercent(discountPercentage);
        product.setSellingPrice(req.getSellingPrice());
        product.setImages(req.getImages());
        product.setMrpPrice(req.getMrpPrice());
        product.setSizes(req.getSizes());
        product.setCreatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);

        log.info(
                "Product created successfully. productId={}, sellerId={}",
                savedProduct.getId(),
                seller.getId()
        );

        return savedProduct;
    }

    // CREATE OR FIND CATEGORY

    private Category getOrCreateCategory(
            String categoryId,
            int level,
            Category parentCategory
    ) {

        Category existingCategory =
                categoryRepository.findByCategoryId(categoryId);

        if (existingCategory != null) {
            return existingCategory;
        }

        Category category = new Category();

        category.setCategoryId(categoryId);
        category.setLevel(level);
        category.setName(categoryId.replace("_", " "));
        category.setParentCategory(parentCategory);

        Category savedCategory = categoryRepository.save(category);

        log.debug(
                "Category created. categoryId={}, level={}",
                categoryId,
                level
        );

        return savedCategory;
    }

    // DISCOUNT CALCULATION

    public static int calculateDiscountPercentage(
            double mrpPrice,
            double sellingPrice
    ) {

        validatePrices(mrpPrice, sellingPrice);

        double discount =
                ((mrpPrice - sellingPrice) / mrpPrice) * 100;

        return (int) discount;
    }

    // PRICE VALIDATION

    private static void validatePrices(
            double mrpPrice,
            double sellingPrice
    ) {

        if (!Double.isFinite(mrpPrice)
                || !Double.isFinite(sellingPrice)
                || mrpPrice <= 0
                || sellingPrice < 0
                || sellingPrice > mrpPrice) {

            throw new IllegalArgumentException(
                    "MRP must be greater than zero and selling price "
                            + "must be between zero and MRP"
            );
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    // DELETE PRODUCT

    @Override
    @Transactional
    public void deleteProduct(Long productId)
            throws ProductException {

        Product product = findProductById(productId);

        productRepository.delete(product);

        log.info(
                "Product deleted successfully. productId={}",
                productId
        );
    }

    // UPDATE PRODUCT

    @Override
    @Transactional
    public Product updateProduct(
            Long productId,
            Product product
    ) throws ProductException {

        if (product == null) {
            throw new IllegalArgumentException(
                    "Product update data must not be null"
            );
        }

        Product existingProduct = findProductById(productId);

        if (product.getTitle() != null) {
            existingProduct.setTitle(product.getTitle());
        }

        if (product.getColor() != null) {
            existingProduct.setColor(product.getColor());
        }

        if (product.getDescription() != null) {
            existingProduct.setDescription(
                    product.getDescription()
            );
        }

        if (product.getImages() != null) {
            existingProduct.setImages(product.getImages());
        }

        if (product.getSizes() != null) {
            existingProduct.setSizes(product.getSizes());
        }

        /*
         * Assumption:
         * Product.mrpPrice and Product.sellingPrice are primitive int.
         *
         * Zero means that the caller did not supply a new price.
         * This version therefore cannot explicitly update a price to zero.
         */

        boolean priceChanged =
                product.getMrpPrice() != 0
                        || product.getSellingPrice() != 0;

        if (priceChanged) {

            int mrp = product.getMrpPrice() != 0
                    ? product.getMrpPrice()
                    : existingProduct.getMrpPrice();

            int sellingPrice = product.getSellingPrice() != 0
                    ? product.getSellingPrice()
                    : existingProduct.getSellingPrice();

            validatePrices(mrp, sellingPrice);

            existingProduct.setMrpPrice(mrp);
            existingProduct.setSellingPrice(sellingPrice);

            existingProduct.setDiscountPercent(
                    calculateDiscountPercentage(
                            mrp,
                            sellingPrice
                    )
            );
        }

        Product updatedProduct =
                productRepository.save(existingProduct);

        log.info(
                "Product updated successfully. productId={}",
                productId
        );

        return updatedProduct;
    }

    // FIND PRODUCT BY ID

    @Override
    @Transactional(readOnly = true)
    public Product findProductById(Long id)
            throws ProductException {

        if (id == null) {
            throw new IllegalArgumentException(
                    "Product ID must not be null"
            );
        }

        return productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn(
                            "Product not found. productId={}",
                            id
                    );

                    return new ProductException(
                            "Product not found"
                    );
                });
    }

    // SEARCH PRODUCTS

    @Override
    @Transactional(readOnly = true)
    public List<Product> searchProduct(String query) {

        if (isBlank(query)) {
            log.debug(
                    "Empty product search query; returning no results"
            );

            return List.of();
        }

        return productRepository.searchProduct(query.trim());
    }

    // FILTER, SORT AND PAGINATE PRODUCTS

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getAllProduct(
            String category,
            String brand,
            String color,
            String size,
            Integer minPrice,
            Integer maxPrice,
            Integer minDiscount,
            String sort,
            String stock,
            Integer pageNumber
    ) {

        int page = pageNumber == null ? 0 : pageNumber;

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number must not be negative"
            );
        }

        if ((minPrice != null && minPrice < 0)
                || (maxPrice != null && maxPrice < 0)) {

            throw new IllegalArgumentException(
                    "Price filters must not be negative"
            );
        }

        if (minPrice != null
                && maxPrice != null
                && minPrice > maxPrice) {

            throw new IllegalArgumentException(
                    "Minimum price must not exceed maximum price"
            );
        }

        if (minDiscount != null
                && (minDiscount < 0 || minDiscount > 100)) {

            throw new IllegalArgumentException(
                    "Minimum discount must be between 0 and 100"
            );
        }

        Specification<Product> spec = (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // CATEGORY FILTER

            if (!isBlank(category)) {

                Join<Product, Category> categoryJoin =
                        root.join("category");

                Predicate directCategory = cb.equal(
                        categoryJoin.get("categoryId"),
                        category
                );

                Predicate parentCategory = cb.equal(
                        categoryJoin.get("parentCategory")
                                .get("categoryId"),
                        category
                );

                predicates.add(
                        cb.or(directCategory, parentCategory)
                );
            }

            // COLOR FILTER

            if (!isBlank(color)) {
                predicates.add(
                        cb.equal(root.get("color"), color)
                );
            }

            /*
             * SIZE FILTER
             *
             * This assumes Product has a singular field named "size".
             * If Product uses a collection field named "sizes",
             * adapt this predicate to the entity mapping.
             */

            if (!isBlank(size)) {
                predicates.add(
                        cb.equal(root.get("size"), size)
                );
            }

            // MINIMUM PRICE

            if (minPrice != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.<Integer>get("sellingPrice"),
                                minPrice
                        )
                );
            }

            // MAXIMUM PRICE

            if (maxPrice != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.<Integer>get("sellingPrice"),
                                maxPrice
                        )
                );
            }

            // MINIMUM DISCOUNT

            if (minDiscount != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.<Integer>get("discountPercent"),
                                minDiscount
                        )
                );
            }

            /*
             * STOCK FILTER
             *
             * This assumes Product has a field named "stock"
             * compatible with the supplied String value.
             */

            if (!isBlank(stock)) {
                predicates.add(
                        cb.equal(root.get("stock"), stock)
                );
            }

            /*
             * Brand filtering is not implemented because the
             * Product brand mapping has not been confirmed.
             */

            return cb.and(
                    predicates.toArray(new Predicate[0])
            );
        };

        Sort pageSort = switch (sort == null ? "" : sort) {
            case "price_low" ->
                    Sort.by("sellingPrice").ascending();

            case "price_high" ->
                    Sort.by("sellingPrice").descending();

            default ->
                    Sort.unsorted();
        };

        Pageable pageable = PageRequest.of(
                page,
                10,
                pageSort
        );

        Page<Product> results =
                productRepository.findAll(spec, pageable);

        log.debug(
                "Product search completed. page={}, resultCount={}",
                page,
                results.getNumberOfElements()
        );

        return results;
    }

    // RECENTLY ADDED PRODUCTS

    @Override
    @Transactional(readOnly = true)
    public List<Product> recentlyAddedProduct() {

        return productRepository.findAll(
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
                .stream()
                .limit(10)
                .toList();
    }

    // FIND PRODUCTS BY SELLER

    @Override
    @Transactional(readOnly = true)
    public List<Product> getProductBySellerId(Long sellerId) {

        if (sellerId == null) {
            throw new IllegalArgumentException(
                    "Seller ID must not be null"
            );
        }

        List<Product> products =
                productRepository.findBySellerId(sellerId);

        log.debug(
                "Products retrieved for seller. sellerId={}, count={}",
                sellerId,
                products.size()
        );

        return products;
    }
}