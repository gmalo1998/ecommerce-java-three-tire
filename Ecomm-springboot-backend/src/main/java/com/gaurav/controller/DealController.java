
package com.gaurav.controller;

import com.gaurav.model.Deal;
import com.gaurav.response.ApiResponse;
import com.gaurav.service.DealService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/deals")
public class DealController {

    private final DealService dealService;

    @PostMapping
    public ResponseEntity<Deal> createDeals(
            @RequestBody Deal deal) {

        log.info("Received request to create a deal");

        Deal createdDeal = dealService.createDeal(deal);

        log.info("Deal created successfully");

        return new ResponseEntity<>(createdDeal, HttpStatus.ACCEPTED);
    }

    @GetMapping
    public ResponseEntity<List<Deal>> getDeals() {

        log.debug("Fetching all deals");

        List<Deal> deals = dealService.getDeals();

        log.debug("Successfully fetched {} deals", deals.size());

        return new ResponseEntity<>(deals, HttpStatus.ACCEPTED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Deal> updateDeal(
            @PathVariable Long id,
            @RequestBody Deal deal) throws Exception {

        log.info("Received request to update deal ID: {}", id);

        Deal updatedDeal = dealService.updateDeal(deal, id);

        log.info("Deal updated successfully. Deal ID: {}", id);

        return ResponseEntity.ok(updatedDeal);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteDeals(
            @PathVariable Long id) throws Exception {

        log.info("Received request to delete deal ID: {}", id);

        dealService.deleteDeal(id);

        ApiResponse response = new ApiResponse();
        response.setMessage("Deal deleted");

        log.info("Deal deleted successfully. Deal ID: {}", id);

        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }
}