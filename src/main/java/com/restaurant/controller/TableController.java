package com.restaurant.controller;

import com.restaurant.dto.ApiResponse;
import com.restaurant.model.RestaurantTable;
import com.restaurant.model.TableStatus;
import com.restaurant.service.TableService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
@CrossOrigin(origins = "*")
public class TableController {

    private final TableService tableService;

    @Autowired
    public TableController(TableService tableService) {
        this.tableService = tableService;
    }

    @GetMapping
    public ApiResponse<List<RestaurantTable>> getAll(@RequestParam(defaultValue = "false") boolean availableOnly) {
        return ApiResponse.ok(availableOnly ? tableService.getAvailableNow() : tableService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<RestaurantTable> getOne(@PathVariable Long id) {
        return ApiResponse.ok(tableService.getById(id));
    }

    @PostMapping
    public ApiResponse<RestaurantTable> create(@Valid @RequestBody RestaurantTable table) {
        return ApiResponse.ok("Table created", tableService.create(table));
    }

    @PutMapping("/{id}")
    public ApiResponse<RestaurantTable> update(@PathVariable Long id, @Valid @RequestBody RestaurantTable table) {
        return ApiResponse.ok("Table updated", tableService.update(id, table));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<RestaurantTable> updateStatus(@PathVariable Long id, @RequestParam TableStatus status) {
        return ApiResponse.ok("Table status updated", tableService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        tableService.delete(id);
        return ApiResponse.ok("Table deleted", null);
    }
}
