package com.restaurant.repository;

import com.restaurant.model.MenuCategory;
import com.restaurant.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByCategory(MenuCategory category);
    List<MenuItem> findByAvailableTrue();
}
