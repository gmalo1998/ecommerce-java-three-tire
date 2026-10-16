
package com.gaurav.service.impl;

import com.gaurav.model.HomeCategory;
import com.gaurav.repository.HomeCategoryRepository;
import com.gaurav.service.HomeCategoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeCategoryServiceImpl implements HomeCategoryService {

    private final HomeCategoryRepository homeCategoryRepository;

    @Override
    @Transactional
    public HomeCategory createCategory(HomeCategory category) {
        if (category == null) {
            throw new IllegalArgumentException(
                    "Category details must be provided"
            );
        }

        HomeCategory savedCategory =
                homeCategoryRepository.save(category);

        log.info("Home category created successfully with ID: {}",
                savedCategory.getId());

        return savedCategory;
    }

    @Override
    @Transactional
    public List<HomeCategory> createCategories(
            List<HomeCategory> categories) {

        if (categories == null || categories.isEmpty()) {
            throw new IllegalArgumentException(
                    "Category list must not be null or empty"
            );
        }

        List<HomeCategory> existingCategories =
                homeCategoryRepository.findAll();

        if (existingCategories.isEmpty()) {
            List<HomeCategory> savedCategories =
                    homeCategoryRepository.saveAll(categories);

            log.info("Created {} initial home categories",
                    savedCategories.size());

            return savedCategories;
        }

        log.debug("Initial categories already exist. Returning {} categories",
                existingCategories.size());

        return existingCategories;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HomeCategory> getAllCategories() {
        List<HomeCategory> categories =
                homeCategoryRepository.findAll();

        log.debug("Retrieved {} home categories", categories.size());

        return categories;
    }

    @Override
    @Transactional
    public HomeCategory updateCategory(
            HomeCategory category, Long id) throws Exception {

        if (category == null) {
            throw new IllegalArgumentException(
                    "Category details must be provided"
            );
        }

        HomeCategory existingCategory =
                homeCategoryRepository.findById(id)
                        .orElseThrow(() ->
                                new Exception("Category not found: " + id));

        if (category.getImage() != null) {
            existingCategory.setImage(category.getImage());
        }

        if (category.getCategoryId() != null) {
            existingCategory.setCategoryId(category.getCategoryId());
        }

        HomeCategory updatedCategory =
                homeCategoryRepository.save(existingCategory);

        log.info("Home category updated successfully with ID: {}", id);

        return updatedCategory;
    }
}

