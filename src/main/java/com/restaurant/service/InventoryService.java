package com.restaurant.service;

import com.restaurant.exception.InsufficientInventoryException;
import com.restaurant.exception.ResourceNotFoundException;
import com.restaurant.model.InventoryItem;
import com.restaurant.model.MenuItem;
import com.restaurant.repository.InventoryItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Owns all stock-level logic: checking whether enough ingredients exist to
 * fulfil an order, auto-deducting stock when orders are confirmed, restocking,
 * and surfacing low-stock alerts for the admin dashboard.
 */
@Service
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;

    @Autowired
    public InventoryService(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
    }

    public List<InventoryItem> getAll() {
        return inventoryItemRepository.findAll();
    }

    public InventoryItem getById(Long id) {
        return inventoryItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found: " + id));
    }

    public InventoryItem create(InventoryItem item) {
        return inventoryItemRepository.save(item);
    }

    public List<InventoryItem> getLowStockItems() {
        return inventoryItemRepository.findAll().stream()
                .filter(InventoryItem::isLowStock)
                .toList();
    }

    /**
     * Verifies that enough of every ingredient exists to make the requested
     * quantity of a menu item, BEFORE any stock is touched. Called during
     * order validation so we fail fast instead of partially deducting stock.
     */
    public void assertSufficientStock(MenuItem menuItem, int quantity) {
        for (InventoryItem ingredient : menuItem.getIngredients()) {
            BigDecimal required = BigDecimal.valueOf(quantity); // 1 unit of ingredient per item ordered (simplified recipe model)
            if (ingredient.getQuantityInStock().compareTo(required) < 0) {
                throw new InsufficientInventoryException(
                        "Not enough " + ingredient.getName() + " in stock to make " + quantity + "x " + menuItem.getName()
                                + " (have " + ingredient.getQuantityInStock() + " " + ingredient.getUnit() + ")");
            }
        }
    }

    /**
     * Deducts stock for one order line. Assumes assertSufficientStock already
     * passed for the whole order so this simply commits the deduction.
     */
    @Transactional
    public void deductForOrderItem(MenuItem menuItem, int quantity) {
        for (InventoryItem ingredient : menuItem.getIngredients()) {
            BigDecimal newQty = ingredient.getQuantityInStock().subtract(BigDecimal.valueOf(quantity));
            ingredient.setQuantityInStock(newQty.max(BigDecimal.ZERO));
            inventoryItemRepository.save(ingredient);
        }
    }

    public InventoryItem restock(Long id, BigDecimal quantityToAdd) {
        InventoryItem item = getById(id);
        item.setQuantityInStock(item.getQuantityInStock().add(quantityToAdd));
        return inventoryItemRepository.save(item);
    }

    public InventoryItem adjustStock(Long id, BigDecimal newQuantity) {
        InventoryItem item = getById(id);
        item.setQuantityInStock(newQuantity);
        return inventoryItemRepository.save(item);
    }
}
