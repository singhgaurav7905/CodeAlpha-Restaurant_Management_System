package com.restaurant.controller;

import com.restaurant.dto.ApiResponse;
import com.restaurant.dto.DailySalesReport;
import com.restaurant.model.InventoryItem;
import com.restaurant.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /** Daily sales summary. Defaults to today; pass ?date=2026-08-20 for another day. */
    @GetMapping("/daily-sales")
    public ApiResponse<DailySalesReport> dailySales(@RequestParam(required = false) String date) {
        LocalDate day = date != null ? LocalDate.parse(date) : LocalDate.now();
        return ApiResponse.ok(reportService.getDailySalesReport(day));
    }

    @GetMapping("/stock-alerts")
    public ApiResponse<List<InventoryItem>> stockAlerts() {
        return ApiResponse.ok(reportService.getStockAlerts());
    }
}
