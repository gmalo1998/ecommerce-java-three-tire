
package com.gaurav.service.impl;

import com.gaurav.dto.RevenueChart;
import com.gaurav.model.Order;
import com.gaurav.repository.OrderRepository;
import com.gaurav.service.RevenueService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RevenueServiceImpl implements RevenueService {

    private final OrderRepository orderRepository;

    /**
     * Get daily revenue data for the past X days.
     */
    @Override
    @Transactional(readOnly = true)
    public List<RevenueChart> getDailyRevenueForChart(
            int days,
            Long sellerId
    ) {
        validateInput(days, sellerId, "days");

        log.info(
                "Generating daily revenue chart. sellerId={}, days={}",
                sellerId,
                days
        );

        List<RevenueChart> revenueData = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = currentDate.minusDays(i);

            double dailyRevenue = orderRepository
                    .findBySellerIdAndOrderDateBetween(
                            sellerId,
                            date.atStartOfDay(),
                            date.plusDays(1).atStartOfDay()
                    )
                    .stream()
                    .mapToDouble(Order::getTotalSellingPrice)
                    .sum();

            RevenueChart revenueChart = new RevenueChart();
            revenueChart.setRevenue(dailyRevenue);
            revenueChart.setDate(date.toString());

            revenueData.add(revenueChart);

            log.debug(
                    "Daily revenue calculated. sellerId={}, date={}, revenue={}",
                    sellerId,
                    date,
                    dailyRevenue
            );
        }

        log.info(
                "Daily revenue chart generated successfully. sellerId={}, entries={}",
                sellerId,
                revenueData.size()
        );

        return revenueData;
    }

    /**
     * Get monthly revenue data for the past X months.
     */
    @Override
    @Transactional(readOnly = true)
    public List<RevenueChart> getMonthlyRevenueForChart(
            int months,
            Long sellerId
    ) {
        validateInput(months, sellerId, "months");

        log.info(
                "Generating monthly revenue chart. sellerId={}, months={}",
                sellerId,
                months
        );

        List<RevenueChart> revenueData = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();

        for (int i = months - 1; i >= 0; i--) {
            LocalDate date = currentDate.minusMonths(i);
            LocalDate startOfMonth = date.withDayOfMonth(1);
            LocalDate startOfNextMonth = startOfMonth.plusMonths(1);

            double monthlyRevenue = orderRepository
                    .findBySellerIdAndOrderDateBetween(
                            sellerId,
                            startOfMonth.atStartOfDay(),
                            startOfNextMonth.atStartOfDay()
                    )
                    .stream()
                    .mapToDouble(Order::getTotalSellingPrice)
                    .sum();

            RevenueChart revenueChart = new RevenueChart();
            revenueChart.setRevenue(monthlyRevenue);
            revenueChart.setDate(
                    date.getYear()
                            + "-"
                            + String.format("%02d", date.getMonthValue())
            );

            revenueData.add(revenueChart);

            log.debug(
                    "Monthly revenue calculated. sellerId={}, month={}, revenue={}",
                    sellerId,
                    revenueChart.getDate(),
                    monthlyRevenue
            );
        }

        log.info(
                "Monthly revenue chart generated successfully. sellerId={}, entries={}",
                sellerId,
                revenueData.size()
        );

        return revenueData;
    }

    /**
     * Get yearly revenue data for the past X years.
     */
    @Override
    @Transactional(readOnly = true)
    public List<RevenueChart> getYearlyRevenueForChart(
            int years,
            Long sellerId
    ) {
        validateInput(years, sellerId, "years");

        log.info(
                "Generating yearly revenue chart. sellerId={}, years={}",
                sellerId,
                years
        );

        List<RevenueChart> revenueData = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();

        for (int i = years - 1; i >= 0; i--) {
            LocalDate startOfYear =
                    currentDate.minusYears(i).withDayOfYear(1);

            LocalDate startOfNextYear = startOfYear.plusYears(1);

            double yearlyRevenue = orderRepository
                    .findBySellerIdAndOrderDateBetween(
                            sellerId,
                            startOfYear.atStartOfDay(),
                            startOfNextYear.atStartOfDay()
                    )
                    .stream()
                    .mapToDouble(Order::getTotalSellingPrice)
                    .sum();

            RevenueChart revenueChart = new RevenueChart();
            revenueChart.setRevenue(yearlyRevenue);
            revenueChart.setDate(
                    String.valueOf(startOfYear.getYear())
            );

            revenueData.add(revenueChart);

            log.debug(
                    "Yearly revenue calculated. sellerId={}, year={}, revenue={}",
                    sellerId,
                    revenueChart.getDate(),
                    yearlyRevenue
            );
        }

        log.info(
                "Yearly revenue chart generated successfully. sellerId={}, entries={}",
                sellerId,
                revenueData.size()
        );

        return revenueData;
    }

    /**
     * Get hourly revenue data for the current day.
     */
    @Override
    @Transactional(readOnly = true)
    public List<RevenueChart> getHourlyRevenueForChart(Long sellerId) {
        validateInput(1, sellerId, "hours");

        log.info(
                "Generating hourly revenue chart for current day. sellerId={}",
                sellerId
        );

        List<RevenueChart> revenueData = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        LocalDateTime startOfDay = currentDate.atStartOfDay();

        for (int i = 0; i < 24; i++) {
            LocalDateTime startOfHour = startOfDay.plusHours(i);
            LocalDateTime startOfNextHour = startOfHour.plusHours(1);

            double hourlyRevenue = orderRepository
                    .findBySellerIdAndOrderDateBetween(
                            sellerId,
                            startOfHour,
                            startOfNextHour
                    )
                    .stream()
                    .mapToDouble(Order::getTotalSellingPrice)
                    .sum();

            RevenueChart revenueChart = new RevenueChart();
            revenueChart.setRevenue(hourlyRevenue);
            revenueChart.setDate(
                    String.format("%02d:00", startOfHour.getHour())
            );

            revenueData.add(revenueChart);

            log.debug(
                    "Hourly revenue calculated. sellerId={}, hour={}, revenue={}",
                    sellerId,
                    revenueChart.getDate(),
                    hourlyRevenue
            );
        }

        log.info(
                "Hourly revenue chart generated successfully. sellerId={}, entries={}",
                sellerId,
                revenueData.size()
        );

        return revenueData;
    }

    /**
     * Select revenue chart based on the requested type.
     */
    @Override
    @Transactional(readOnly = true)
    public List<RevenueChart> getRevenueChartByType(
            String type,
            Long sellerId
    ) {
        if (sellerId == null) {
            log.warn("Revenue chart request rejected because sellerId is null");
            throw new IllegalArgumentException(
                    "Seller ID must not be null"
            );
        }

        if (type == null || type.isBlank()) {
            log.warn(
                    "Revenue chart request rejected because chart type is blank. sellerId={}",
                    sellerId
            );
            throw new IllegalArgumentException(
                    "Chart type must not be blank"
            );
        }

        log.info(
                "Revenue chart requested. sellerId={}, type={}",
                sellerId,
                type
        );

        return switch (type.toLowerCase()) {
            case "monthly" -> getMonthlyRevenueForChart(12, sellerId);
            case "daily" -> getDailyRevenueForChart(30, sellerId);
            case "yearly" -> getYearlyRevenueForChart(5, sellerId);
            case "hourly" -> getHourlyRevenueForChart(sellerId);
            default -> {
                log.warn(
                        "Unsupported revenue chart type. sellerId={}, type={}",
                        sellerId,
                        type
                );
                throw new IllegalArgumentException(
                        "Unsupported chart type: " + type
                );
            }
        };
    }

    private void validateInput(
            int period,
            Long sellerId,
            String periodName
    ) {
        if (sellerId == null) {
            log.warn(
                    "Revenue chart request rejected: sellerId is null"
            );
            throw new IllegalArgumentException(
                    "Seller ID must not be null"
            );
        }

        if (period <= 0) {
            log.warn(
                    "Revenue chart request rejected. sellerId={}, {}={}",
                    sellerId,
                    periodName,
                    period
            );
            throw new IllegalArgumentException(
                    periodName + " must be greater than zero"
            );
        }
    }
}

