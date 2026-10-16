
package com.gaurav.service.impl;

import com.gaurav.model.Deal;
import com.gaurav.model.HomeCategory;
import com.gaurav.repository.DealRepository;
import com.gaurav.repository.HomeCategoryRepository;
import com.gaurav.service.DealService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealServiceImpl implements DealService {

    private final DealRepository dealRepository;
    private final HomeCategoryRepository homeCategoryRepository;

    @Override
    @Transactional
    public Deal createDeal(Deal deal) {
        if (deal == null
                || deal.getCategory() == null
                || deal.getCategory().getId() == null) {
            throw new IllegalArgumentException(
                    "Deal and category ID must be provided"
            );
        }

        Long categoryId = deal.getCategory().getId();

        HomeCategory category = homeCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Home category not found: " + categoryId
                ));

        Deal newDeal = new Deal();
        newDeal.setCategory(category);
        newDeal.setDiscount(deal.getDiscount());

        Deal savedDeal = dealRepository.save(newDeal);

        log.info("Deal created successfully with ID: {}", savedDeal.getId());

        return savedDeal;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Deal> getDeals() {
        List<Deal> deals = dealRepository.findAll();

        log.debug("Retrieved {} deals", deals.size());

        return deals;
    }

    @Override
    @Transactional
    public Deal updateDeal(Deal deal, Long id) throws Exception {
        if (deal == null) {
            throw new IllegalArgumentException("Deal details must be provided");
        }

        Deal existingDeal = dealRepository.findById(id)
                .orElseThrow(() -> new Exception("Deal not found: " + id));

        if (deal.getDiscount() != null) {
            existingDeal.setDiscount(deal.getDiscount());
        }

        if (deal.getCategory() != null
                && deal.getCategory().getId() != null) {

            Long categoryId = deal.getCategory().getId();

            HomeCategory category = homeCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Home category not found: " + categoryId
                    ));

            existingDeal.setCategory(category);
        }

        Deal updatedDeal = dealRepository.save(existingDeal);

        log.info("Deal updated successfully with ID: {}", id);

        return updatedDeal;
    }

    @Override
    @Transactional
    public void deleteDeal(Long id) throws Exception {
        Deal deal = dealRepository.findById(id)
                .orElseThrow(() -> new Exception("Deal not found: " + id));

        dealRepository.delete(deal);

        log.info("Deal deleted successfully with ID: {}", id);
    }
}

