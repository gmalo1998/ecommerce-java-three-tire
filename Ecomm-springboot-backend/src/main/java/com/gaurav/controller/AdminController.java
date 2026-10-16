
package com.gaurav.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gaurav.domain.AccountStatus;
import com.gaurav.exception.SellerException;
import com.gaurav.model.HomeCategory;
import com.gaurav.model.Seller;
import com.gaurav.service.HomeCategoryService;
import com.gaurav.service.SellerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private static final Logger logger =
            LoggerFactory.getLogger(AdminController.class);

    private final SellerService sellerService;
    private final HomeCategoryService homeCategoryService;

    @PatchMapping("/seller/{id}/status/{status}")
    public ResponseEntity<Seller> updateSellerStatus(
            @PathVariable Long id,
            @PathVariable AccountStatus status) throws SellerException {

        Seller updatedSeller =
                sellerService.updateSellerAccountStatus(id, status);

        logger.info("Seller account status updated successfully. Seller ID: {}, Status: {}",
                id, status);

        return ResponseEntity.ok(updatedSeller);
    }

    @GetMapping("/home-category")
    public ResponseEntity<List<HomeCategory>> getHomeCategory()
            throws Exception {

        List<HomeCategory> categories =
                homeCategoryService.getAllCategories();

        logger.debug("Retrieved {} home categories", categories.size());

        return ResponseEntity.ok(categories);
    }

    @PatchMapping("/home-category/{id}")
    public ResponseEntity<HomeCategory> updateHomeCategory(
            @PathVariable Long id,
            @RequestBody HomeCategory homeCategory) throws Exception {

        HomeCategory updatedCategory =
                homeCategoryService.updateCategory(homeCategory, id);

        logger.info("Home category updated successfully. Category ID: {}", id);

        return ResponseEntity.ok(updatedCategory);
    }
}