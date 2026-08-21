package com.restaurant.service;

import com.restaurant.exception.ResourceNotFoundException;
import com.restaurant.model.RestaurantTable;
import com.restaurant.model.TableStatus;
import com.restaurant.repository.RestaurantTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TableService {

    private final RestaurantTableRepository tableRepository;

    @Autowired
    public TableService(RestaurantTableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }

    public List<RestaurantTable> getAll() {
        return tableRepository.findAll();
    }

    public List<RestaurantTable> getAvailableNow() {
        return tableRepository.findByStatus(TableStatus.AVAILABLE);
    }

    public RestaurantTable getById(Long id) {
        return tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found: " + id));
    }

    public RestaurantTable create(RestaurantTable table) {
        return tableRepository.save(table);
    }

    public RestaurantTable updateStatus(Long id, TableStatus status) {
        RestaurantTable table = getById(id);
        table.markStatus(status);
        return tableRepository.save(table);
    }

    public RestaurantTable update(Long id, RestaurantTable updated) {
        RestaurantTable existing = getById(id);
        existing.setTableNumber(updated.getTableNumber());
        existing.setCapacity(updated.getCapacity());
        existing.setLocation(updated.getLocation());
        return tableRepository.save(existing);
    }

    public void delete(Long id) {
        tableRepository.delete(getById(id));
    }
}
