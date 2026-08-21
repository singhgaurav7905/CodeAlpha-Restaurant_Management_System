package com.restaurant.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Payload for PUT /api/inventory/{id}/restock or /adjust. */
public class InventoryUpdateRequest {

    @NotNull
    private BigDecimal quantity;

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
}
