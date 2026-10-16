
package com.gaurav.controller;

import com.gaurav.model.Home;
import com.gaurav.model.HomeCategory;
import com.gaurav.service.HomeCategoryService;
import com.gaurav.service.HomeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CustomerController {

    private final HomeCategoryService homeCategoryService;
    private final HomeService homeService;

    @GetMapping("/home-page")
    public ResponseEntity<Home> getHomePageData() {

        log.debug("Fetching home page data");

        Home homePageData = homeService.getHomePageData();

        log.debug("Home page data fetched successfully");

        return ResponseEntity.ok(homePageData);
    }

    @PostMapping("/home/categories")
    public ResponseEntity<Home> createHomeCategories(
            @RequestBody List<HomeCategory> homeCategories) {

        log.info(
                "Request received to create home categories. Category count: {}",
                homeCategories == null ? 0 : homeCategories.size()
        );

        if (homeCategories == null || homeCategories.isEmpty()) {
            log.warn("Home category creation rejected because the request is empty");

            return ResponseEntity.badRequest().build();
        }

        List<HomeCategory> categories =
                homeCategoryService.createCategories(homeCategories);

        Home home = homeService.creatHomePageData(categories);

        log.info(
                "Home page categories created successfully. Category count: {}",
                categories.size()
        );

        return new ResponseEntity<>(home, HttpStatus.CREATED);
    }
}