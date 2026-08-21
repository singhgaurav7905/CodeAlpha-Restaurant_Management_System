package com.restaurant.service;

import com.restaurant.dto.DailySalesReport;
import com.restaurant.model.InventoryItem;
import com.restaurant.model.Order;
import com.restaurant.model.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Produces the admin dashboard's reporting views: daily sales and stock alerts. */
@Service
public class ReportService {

    private final OrderService orderService;
    private final InventoryService inventoryService;

    @Autowired
    public ReportService(OrderService orderService, InventoryService inventoryService) {
        this.orderService = orderService;
        this.inventoryService = inventoryService;
    }

    public DailySalesReport getDailySalesReport(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        List<Order> ordersToday = orderService.getOrdersBetween(start, end);

        DailySalesReport report = new DailySalesReport();
        report.setDate(date);
        report.setTotalOrders(ordersToday.size());

        long completed = ordersToday.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count();
        long cancelled = ordersToday.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();
        report.setCompletedOrders(completed);
        report.setCancelledOrders(cancelled);

        BigDecimal revenue = ordersToday.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setTotalRevenue(revenue);

        long revenueOrderCount = ordersToday.stream().filter(o -> o.getStatus() != OrderStatus.CANCELLED).count();
        report.setAverageOrderValue(revenueOrderCount == 0
                ? BigDecimal.ZERO
                : revenue.divide(BigDecimal.valueOf(revenueOrderCount), 2, RoundingMode.HALF_UP));

        Map<String, Long> topItems = ordersToday.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .flatMap(o -> o.getItems().stream())
                .collect(Collectors.groupingBy(oi -> oi.getMenuItem().getName(),
                        LinkedHashMap::new,
                        Collectors.summingLong(oi -> oi.getQuantity())));
        Map<String, Long> sortedTopItems = topItems.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));
        report.setTopSellingItems(sortedTopItems);

        report.setLowStockAlerts(inventoryService.getLowStockItems().stream()
                .map(i -> i.getName() + " - " + i.getQuantityInStock() + " " + i.getUnit() + " left (reorder at " + i.getReorderThreshold() + ")")
                .sorted()
                .toList());

        return report;
    }

    public List<InventoryItem> getStockAlerts() {
        return inventoryService.getLowStockItems().stream()
                .sorted(Comparator.comparing(InventoryItem::getName))
                .toList();
    }
}
