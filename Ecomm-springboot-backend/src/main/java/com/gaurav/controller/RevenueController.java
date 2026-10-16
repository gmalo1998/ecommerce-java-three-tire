
package com.gaurav.controller;

import com.gaurav.dto.RevenueChart;
import com.gaurav.exception.SellerException;
import com.gaurav.model.Seller;
import com.gaurav.service.RevenueService;
import com.gaurav.service.SellerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller/revenue/chart")
public class RevenueController {

    private final RevenueService revenueService;
    private final SellerService sellerService;

    @GetMapping
    public ResponseEntity<List<RevenueChart>> getRevenueChart(
            @RequestParam(defaultValue = "today") String type,
            @RequestHeader("Authorization") String jwt) throws SellerException {

        log.debug("Revenue chart requested. Type: {}", type);

        Seller seller = sellerService.getSellerProfile(jwt);

        List<RevenueChart> revenue =
                revenueService.getRevenueChartByType(type, seller.getId());

        log.debug("Revenue chart retrieved for seller ID: {}. Data points: {}",
                seller.getId(), revenue.size());

        return ResponseEntity.ok(revenue);
    }
}