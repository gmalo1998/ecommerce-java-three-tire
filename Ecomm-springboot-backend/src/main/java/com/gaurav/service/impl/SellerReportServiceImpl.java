
package com.gaurav.service.impl;

import com.gaurav.model.Seller;
import com.gaurav.model.SellerReport;
import com.gaurav.repository.SellerReportRepository;
import com.gaurav.service.SellerReportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerReportServiceImpl implements SellerReportService {

    private final SellerReportRepository sellerReportRepository;

    /**
     * Retrieves the seller report.
     * Creates and saves a new report if one does not exist.
     */
    @Override
    @Transactional
    public SellerReport getSellerReport(Seller seller) {

        if (seller == null || seller.getId() == null) {
            log.warn("Cannot retrieve seller report: seller or seller ID is null");
            throw new IllegalArgumentException(
                    "Valid seller is required"
            );
        }

        Long sellerId = seller.getId();

        log.debug("Fetching seller report for seller ID: {}", sellerId);

        SellerReport report =
                sellerReportRepository.findBySellerId(sellerId);

        if (report != null) {
            log.info("Existing seller report found for seller ID: {}", sellerId);
            return report;
        }

        log.info("No seller report found for seller ID: {}. Creating a new report",
                sellerId);

        SellerReport newReport = new SellerReport();
        newReport.setSeller(seller);

        SellerReport savedReport =
                sellerReportRepository.save(newReport);

        log.info("Seller report created successfully for seller ID: {}",
                sellerId);

        return savedReport;
    }

    /**
     * Saves updates to a seller report.
     */
    @Override
    @Transactional
    public SellerReport updateSellerReport(SellerReport sellerReport) {

        if (sellerReport == null) {
            log.warn("Cannot update seller report: report is null");
            throw new IllegalArgumentException(
                    "Seller report must not be null"
            );
        }

        if (sellerReport.getSeller() == null
                || sellerReport.getSeller().getId() == null) {
            log.warn("Cannot update seller report: seller or seller ID is null");
            throw new IllegalArgumentException(
                    "Valid seller is required for the report"
            );
        }

        Long sellerId = sellerReport.getSeller().getId();

        log.debug("Updating seller report for seller ID: {}", sellerId);

        SellerReport updatedReport =
                sellerReportRepository.save(sellerReport);

        log.info("Seller report updated successfully for seller ID: {}",
                sellerId);

        return updatedReport;
    }
}

