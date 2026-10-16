
package com.gaurav.service.impl;

import com.gaurav.dto.RevenueChart;
import com.gaurav.model.Order;
import com.gaurav.repository.OrderRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RevenueServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private RevenueServiceImpl revenueService;

    /**
     * Configure the repository to return no orders.
     * Call this helper only in tests that invoke the repository.
     */
    private void returnNoOrders() {
        when(orderRepository.findBySellerIdAndOrderDateBetween(
                anyLong(),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(Collections.<Order>emptyList());
    }

    // DAILY REVENUE

    @Test
    void getDailyRevenueForChart_shouldReturnRequestedNumberOfDays() {
        returnNoOrders();

        List<RevenueChart> result =
                revenueService.getDailyRevenueForChart(30, 1L);

        assertNotNull(result);
        assertEquals(30, result.size());

        assertEquals(
                LocalDate.now().minusDays(29).toString(),
                result.get(0).getDate()
        );

        assertEquals(
                LocalDate.now().toString(),
                result.get(29).getDate()
        );

        assertEquals(0.0, result.get(0).getRevenue(), 0.001);

        verify(orderRepository, times(30))
                .findBySellerIdAndOrderDateBetween(
                        eq(1L),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void getDailyRevenueForChart_shouldRejectZeroDays() {
        assertThrows(
                IllegalArgumentException.class,
                () -> revenueService.getDailyRevenueForChart(0, 1L)
        );

        verifyNoInteractions(orderRepository);
    }

    @Test
    void getDailyRevenueForChart_shouldUseTheProvidedSellerId() {
        returnNoOrders();

        revenueService.getDailyRevenueForChart(1, 99L);

        verify(orderRepository).findBySellerIdAndOrderDateBetween(
                eq(99L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        );
    }

    // MONTHLY REVENUE

    @Test
    void getMonthlyRevenueForChart_shouldReturnRequestedNumberOfMonths() {
        returnNoOrders();

        List<RevenueChart> result =
                revenueService.getMonthlyRevenueForChart(12, 1L);

        assertNotNull(result);
        assertEquals(12, result.size());

        LocalDate currentDate = LocalDate.now();
        LocalDate firstMonth = currentDate.minusMonths(11);

        assertEquals(
                firstMonth.getYear() + "-"
                        + String.format("%02d", firstMonth.getMonthValue()),
                result.get(0).getDate()
        );

        assertEquals(
                currentDate.getYear() + "-"
                        + String.format("%02d", currentDate.getMonthValue()),
                result.get(11).getDate()
        );

        assertEquals(0.0, result.get(0).getRevenue(), 0.001);

        verify(orderRepository, times(12))
                .findBySellerIdAndOrderDateBetween(
                        eq(1L),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void getMonthlyRevenueForChart_shouldRejectZeroMonths() {
        assertThrows(
                IllegalArgumentException.class,
                () -> revenueService.getMonthlyRevenueForChart(0, 1L)
        );

        verifyNoInteractions(orderRepository);
    }

    // YEARLY REVENUE

    @Test
    void getYearlyRevenueForChart_shouldReturnRequestedNumberOfYears() {
        returnNoOrders();

        List<RevenueChart> result =
                revenueService.getYearlyRevenueForChart(5, 1L);

        assertNotNull(result);
        assertEquals(5, result.size());

        int currentYear = LocalDate.now().getYear();

        assertEquals(
                String.valueOf(currentYear - 4),
                result.get(0).getDate()
        );

        assertEquals(
                String.valueOf(currentYear),
                result.get(4).getDate()
        );

        assertEquals(0.0, result.get(0).getRevenue(), 0.001);

        verify(orderRepository, times(5))
                .findBySellerIdAndOrderDateBetween(
                        eq(1L),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void getYearlyRevenueForChart_shouldRejectZeroYears() {
        assertThrows(
                IllegalArgumentException.class,
                () -> revenueService.getYearlyRevenueForChart(0, 1L)
        );

        verifyNoInteractions(orderRepository);
    }

    // HOURLY REVENUE

    @Test
    void getHourlyRevenueForChart_shouldReturn24Hours() {
        returnNoOrders();

        List<RevenueChart> result =
                revenueService.getHourlyRevenueForChart(1L);

        assertNotNull(result);
        assertEquals(24, result.size());

        assertEquals("00:00", result.get(0).getDate());
        assertEquals("01:00", result.get(1).getDate());
        assertEquals("23:00", result.get(23).getDate());

        assertEquals(0.0, result.get(0).getRevenue(), 0.001);

        verify(orderRepository, times(24))
                .findBySellerIdAndOrderDateBetween(
                        eq(1L),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                );
    }

    // REVENUE CHART BY TYPE

    @Test
    void getRevenueChartByType_shouldReturnMonthlyData() {
        returnNoOrders();

        List<RevenueChart> result =
                revenueService.getRevenueChartByType("monthly", 1L);

        assertNotNull(result);
        assertEquals(12, result.size());

        verify(orderRepository, times(12))
                .findBySellerIdAndOrderDateBetween(
                        eq(1L),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void getRevenueChartByType_shouldReturnDailyData() {
        returnNoOrders();

        List<RevenueChart> result =
                revenueService.getRevenueChartByType("daily", 1L);

        assertNotNull(result);
        assertEquals(30, result.size());

        verify(orderRepository, times(30))
                .findBySellerIdAndOrderDateBetween(
                        eq(1L),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void getRevenueChartByType_shouldReturnHourlyDataForOtherTypes() {
        returnNoOrders();

        List<RevenueChart> result =
                revenueService.getRevenueChartByType("hourly", 1L);

        assertNotNull(result);
        assertEquals(24, result.size());

        verify(orderRepository, times(24))
                .findBySellerIdAndOrderDateBetween(
                        eq(1L),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                );
    }
}

