package com.restaurant.controller;

import com.restaurant.dto.ApiResponse;
import com.restaurant.dto.InventoryUpdateRequest;
import com.restaurant.model.InventoryItem;
import com.restaurant.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ApiResponse<List<InventoryItem>> getAll(@RequestParam(defaultValue = "false") boolean lowStockOnly) {
        return ApiResponse.ok(lowStockOnly ? inventoryService.getLowStockItems() : inventoryService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<InventoryItem> getOne(@PathVariable Long id) {
        return ApiResponse.ok(inventoryService.getById(id));
    }

    @PostMapping
    public ApiResponse<InventoryItem> create(@Valid @RequestBody InventoryItem item) {
        return ApiResponse.ok("Inventory item created", inventoryService.create(item));
    }

    /** Adds stock (e.g. a delivery arrived). */
    @PatchMapping("/{id}/restock")
    public ApiResponse<InventoryItem> restock(@PathVariable Long id, @Valid @RequestBody InventoryUpdateRequest request) {
        return ApiResponse.ok("Stock replenished", inventoryService.restock(id, request.getQuantity()));
    }

    /** Sets the absolute stock level (e.g. after a manual stock count). */
    @PatchMapping("/{id}/adjust")
    public ApiResponse<InventoryItem> adjust(@PathVariable Long id, @Valid @RequestBody InventoryUpdateRequest request) {
        return ApiResponse.ok("Stock adjusted", inventoryService.adjustStock(id, request.getQuantity()));
    }
}
