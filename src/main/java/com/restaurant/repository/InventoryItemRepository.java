package com.restaurant.repository;

import com.restaurant.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    List<InventoryItem> findByQuantityInStockLessThanEqual(java.math.BigDecimal threshold);
}
