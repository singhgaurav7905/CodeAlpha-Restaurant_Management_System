package com.restaurant.controller;

import com.restaurant.dto.ApiResponse;
import com.restaurant.model.MenuCategory;
import com.restaurant.model.MenuItem;
import com.restaurant.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@CrossOrigin(origins = "*")
public class MenuController {

    private final MenuService menuService;

    @Autowired
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /** Public: full menu, or only currently available items with ?availableOnly=true */
    @GetMapping
    public ApiResponse<List<MenuItem>> getMenu(@RequestParam(required = false) MenuCategory category,
                                                @RequestParam(defaultValue = "false") boolean availableOnly) {
        List<MenuItem> items;
        if (category != null) {
            items = menuService.getByCategory(category);
        } else if (availableOnly) {
            items = menuService.getAvailableMenu();
        } else {
            items = menuService.getFullMenu();
        }
        return ApiResponse.ok(items);
    }

    @GetMapping("/{id}")
    public ApiResponse<MenuItem> getOne(@PathVariable Long id) {
        return ApiResponse.ok(menuService.getById(id));
    }

    @PostMapping
    public ApiResponse<MenuItem> create(@Valid @RequestBody MenuItem item) {
        return ApiResponse.ok("Menu item created", menuService.create(item));
    }

    @PutMapping("/{id}")
    public ApiResponse<MenuItem> update(@PathVariable Long id, @Valid @RequestBody MenuItem item) {
        return ApiResponse.ok("Menu item updated", menuService.update(id, item));
    }

    @PatchMapping("/{id}/availability")
    public ApiResponse<MenuItem> setAvailability(@PathVariable Long id, @RequestParam boolean available) {
        return ApiResponse.ok(menuService.setAvailability(id, available));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return ApiResponse.ok("Menu item deleted", null);
    }
}
