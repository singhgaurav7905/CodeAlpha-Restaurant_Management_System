package com.restaurant.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Aggregated sales figures for a single business day. */
public class DailySalesReport {
    private LocalDate date;
    private long totalOrders;
    private long completedOrders;
    private long cancelledOrders;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private Map<String, Long> topSellingItems; // menu item name -> qty sold
    private List<String> lowStockAlerts;

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }

    public long getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(long completedOrders) { this.completedOrders = completedOrders; }

    public long getCancelledOrders() { return cancelledOrders; }
    public void setCancelledOrders(long cancelledOrders) { this.cancelledOrders = cancelledOrders; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public BigDecimal getAverageOrderValue() { return averageOrderValue; }
    public void setAverageOrderValue(BigDecimal averageOrderValue) { this.averageOrderValue = averageOrderValue; }

    public Map<String, Long> getTopSellingItems() { return topSellingItems; }
    public void setTopSellingItems(Map<String, Long> topSellingItems) { this.topSellingItems = topSellingItems; }

    public List<String> getLowStockAlerts() { return lowStockAlerts; }
    public void setLowStockAlerts(List<String> lowStockAlerts) { this.lowStockAlerts = lowStockAlerts; }
}
