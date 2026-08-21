package com.restaurant.repository;

import com.restaurant.model.RestaurantTable;
import com.restaurant.model.TableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {
    List<RestaurantTable> findByStatus(TableStatus status);
}
