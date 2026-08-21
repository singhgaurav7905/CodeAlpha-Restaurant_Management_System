package com.restaurant.controller;

import com.restaurant.dto.ApiResponse;
import com.restaurant.dto.OrderRequest;
import com.restaurant.model.Order;
import com.restaurant.model.OrderStatus;
import com.restaurant.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ApiResponse<List<Order>> getAll(@RequestParam(required = false) OrderStatus status,
                                            @RequestParam(defaultValue = "false") boolean activeOnly) {
        if (status != null) {
            return ApiResponse.ok(orderService.getAll().stream().filter(o -> o.getStatus() == status).toList());
        }
        if (activeOnly) {
            return ApiResponse.ok(orderService.getActiveOrders());
        }
        return ApiResponse.ok(orderService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<Order> getOne(@PathVariable Long id) {
        return ApiResponse.ok(orderService.getById(id));
    }

    /** Places an order: validates stock + item availability, deducts inventory, occupies the table. */
    @PostMapping
    public ApiResponse<Order> placeOrder(@Valid @RequestBody OrderRequest request) {
        return ApiResponse.ok("Order placed", orderService.placeOrder(request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Order> updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        return ApiResponse.ok("Order status updated", orderService.updateStatus(id, status));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Order> cancel(@PathVariable Long id) {
        return ApiResponse.ok("Order cancelled", orderService.cancelOrder(id));
    }
}
