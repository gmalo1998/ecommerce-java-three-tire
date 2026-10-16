
package com.gaurav.service.impl;

import com.gaurav.domain.HomeCategorySection;
import com.gaurav.model.Deal;
import com.gaurav.model.Home;
import com.gaurav.model.HomeCategory;
import com.gaurav.repository.DealRepository;
import com.gaurav.service.HomeCategoryService;
import com.gaurav.service.HomeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private static final int DEFAULT_DEAL_DISCOUNT = 10;

    private final DealRepository dealRepository;
    private final HomeCategoryService homeCategoryService;

    @Override
    @Transactional
    public Home creatHomePageData(List<HomeCategory> allCategories) {

        if (allCategories == null) {
            throw new IllegalArgumentException(
                    "Home categories must not be null"
            );
        }

        List<HomeCategory> gridCategories = allCategories.stream()
                .filter(category ->
                        category.getSection() == HomeCategorySection.GRID)
                .toList();

        List<HomeCategory> shopByCategories = allCategories.stream()
                .filter(category ->
                        category.getSection()
                                == HomeCategorySection.SHOP_BY_CATEGORIES)
                .toList();

        List<HomeCategory> electricCategories = allCategories.stream()
                .filter(category ->
                        category.getSection()
                                == HomeCategorySection.ELECTRIC_CATEGORIES)
                .toList();

        List<HomeCategory> dealCategories = allCategories.stream()
                .filter(category ->
                        category.getSection() == HomeCategorySection.DEALS)
                .toList();

        List<Deal> existingDeals = dealRepository.findAll();
        List<Deal> homepageDeals;

        if (existingDeals.isEmpty()) {

            List<Deal> defaultDeals = dealCategories.stream()
                    .map(category ->
                            new Deal(null, DEFAULT_DEAL_DISCOUNT, category))
                    .toList();

            homepageDeals = defaultDeals.isEmpty()
                    ? List.of()
                    : dealRepository.saveAll(defaultDeals);

            log.info(
                    "Initialized {} default homepage deals",
                    homepageDeals.size()
            );

        } else {

            homepageDeals = existingDeals;

            log.debug(
                    "Using {} existing homepage deals",
                    homepageDeals.size()
            );
        }

        Home home = new Home();

        home.setGrid(gridCategories);
        home.setShopByCategories(shopByCategories);
        home.setElectricCategories(electricCategories);
        home.setDeals(homepageDeals);
        home.setDealCategories(dealCategories);

        log.debug(
                "Homepage data prepared: grid={}, shopBy={}, electric={}, dealCategories={}, deals={}",
                gridCategories.size(),
                shopByCategories.size(),
                electricCategories.size(),
                dealCategories.size(),
                homepageDeals.size()
        );

        return home;
    }

    @Override
    @Transactional(readOnly = true)
    public Home getHomePageData() {

        log.debug("Fetching homepage data");

        List<HomeCategory> allCategories =
                homeCategoryService.getAllCategories();

        List<HomeCategory> gridCategories = allCategories.stream()
                .filter(category ->
                        category.getSection() == HomeCategorySection.GRID)
                .toList();

        List<HomeCategory> shopByCategories = allCategories.stream()
                .filter(category ->
                        category.getSection()
                                == HomeCategorySection.SHOP_BY_CATEGORIES)
                .toList();

        List<HomeCategory> electricCategories = allCategories.stream()
                .filter(category ->
                        category.getSection()
                                == HomeCategorySection.ELECTRIC_CATEGORIES)
                .toList();

        List<HomeCategory> dealCategories = allCategories.stream()
                .filter(category ->
                        category.getSection() == HomeCategorySection.DEALS)
                .toList();

        List<Deal> homepageDeals = dealRepository.findAll();

        Home home = new Home();

        home.setGrid(gridCategories);
        home.setShopByCategories(shopByCategories);
        home.setElectricCategories(electricCategories);
        home.setDeals(homepageDeals);
        home.setDealCategories(dealCategories);

        log.debug("Homepage data fetched successfully");

        return home;
    }
}

