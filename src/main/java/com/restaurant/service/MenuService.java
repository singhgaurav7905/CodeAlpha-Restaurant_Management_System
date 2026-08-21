package com.restaurant.service;

import com.restaurant.exception.ResourceNotFoundException;
import com.restaurant.model.MenuCategory;
import com.restaurant.model.MenuItem;
import com.restaurant.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuService {

    private final MenuItemRepository menuItemRepository;

    @Autowired
    public MenuService(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    public List<MenuItem> getFullMenu() {
        return menuItemRepository.findAll();
    }

    public List<MenuItem> getAvailableMenu() {
        return menuItemRepository.findByAvailableTrue();
    }

    public List<MenuItem> getByCategory(MenuCategory category) {
        return menuItemRepository.findByCategory(category);
    }

    public MenuItem getById(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + id));
    }

    public MenuItem create(MenuItem item) {
        return menuItemRepository.save(item);
    }

    public MenuItem update(Long id, MenuItem updated) {
        MenuItem existing = getById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setPrice(updated.getPrice());
        existing.setCategory(updated.getCategory());
        existing.setVegetarian(updated.isVegetarian());
        existing.setAvailable(updated.isAvailable());
        existing.setImageUrl(updated.getImageUrl());
        return menuItemRepository.save(existing);
    }

    public MenuItem setAvailability(Long id, boolean available) {
        MenuItem item = getById(id);
        item.setAvailable(available);
        return menuItemRepository.save(item);
    }

    public void delete(Long id) {
        MenuItem item = getById(id);
        menuItemRepository.delete(item);
    }
}
