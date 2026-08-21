package com.restaurant.service;

import com.restaurant.dto.OrderItemRequest;
import com.restaurant.dto.OrderRequest;
import com.restaurant.exception.ResourceNotFoundException;
import com.restaurant.exception.TableUnavailableException;
import com.restaurant.model.*;
import com.restaurant.repository.MenuItemRepository;
import com.restaurant.repository.OrderRepository;
import com.restaurant.repository.RestaurantTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Core order-processing logic:
 *  1. validate every requested menu item is available and in stock
 *  2. persist the order + line items with a price snapshot
 *  3. deduct inventory
 *  4. flip the dine-in table to OCCUPIED
 *  5. enforce a sane order-status state machine
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantTableRepository tableRepository;
    private final InventoryService inventoryService;

    // Allowed forward transitions for OrderStatus. Cancellation is allowed from any non-terminal state (handled separately).
    private static final Set<OrderStatus> TERMINAL = EnumSet.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED);

    @Autowired
    public OrderService(OrderRepository orderRepository,
                         MenuItemRepository menuItemRepository,
                         RestaurantTableRepository tableRepository,
                         InventoryService inventoryService) {
        this.orderRepository = orderRepository;
        this.menuItemRepository = menuItemRepository;
        this.tableRepository = tableRepository;
        this.inventoryService = inventoryService;
    }

    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    public Order getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    public List<Order> getActiveOrders() {
        return orderRepository.findAll().stream()
                .filter(o -> !TERMINAL.contains(o.getStatus()))
                .toList();
    }

    @Transactional
    public Order placeOrder(OrderRequest request) {
        Order order = new Order();
        order.setOrderType(request.getOrderType());
        order.setCustomerName(request.getCustomerName());
        order.setCustomerPhone(request.getCustomerPhone());

        RestaurantTable table = null;
        if (request.getTableId() != null) {
            table = tableRepository.findById(request.getTableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Table not found: " + request.getTableId()));
            if (request.getOrderType() == OrderType.DINE_IN && table.getStatus() == TableStatus.CLEANING) {
                throw new TableUnavailableException("Table " + table.getTableNumber() + " is being cleaned and isn't ready yet");
            }
            order.setTable(table);
        }

        // Pass 1: resolve items and validate availability + stock BEFORE mutating anything,
        // so a bad line item can never leave the system in a half-updated state.
        for (OrderItemRequest line : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(line.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + line.getMenuItemId()));
            if (!menuItem.isAvailable()) {
                throw new IllegalArgumentException(menuItem.getName() + " is currently unavailable");
            }
            inventoryService.assertSufficientStock(menuItem, line.getQuantity());
        }

        // Pass 2: everything validated, now build the order and deduct stock.
        for (OrderItemRequest line : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(line.getMenuItemId()).orElseThrow();
            OrderItem orderItem = new OrderItem(menuItem, line.getQuantity());
            orderItem.setNotes(line.getNotes());
            order.addItem(orderItem);
            inventoryService.deductForOrderItem(menuItem, line.getQuantity());
        }

        order.recalculateTotal();
        order.setStatus(OrderStatus.PLACED);

        if (table != null && request.getOrderType() == OrderType.DINE_IN) {
            table.markStatus(TableStatus.OCCUPIED);
            tableRepository.save(table);
        }

        return orderRepository.save(order);
    }

    @Transactional
    public Order updateStatus(Long id, OrderStatus newStatus) {
        Order order = getById(id);
        if (TERMINAL.contains(order.getStatus())) {
            throw new IllegalArgumentException("Order " + id + " is already " + order.getStatus() + " and cannot be changed");
        }
        order.setStatus(newStatus);

        // Freeing the table when dine-in order wraps up / is cancelled keeps the table map in sync automatically.
        if (order.getTable() != null && (newStatus == OrderStatus.COMPLETED || newStatus == OrderStatus.CANCELLED)) {
            RestaurantTable table = order.getTable();
            table.markStatus(TableStatus.CLEANING);
            tableRepository.save(table);
        }

        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(Long id) {
        return updateStatus(id, OrderStatus.CANCELLED);
    }

    public List<Order> getOrdersBetween(LocalDateTime start, LocalDateTime end) {
        return orderRepository.findByCreatedAtBetween(start, end);
    }
}
