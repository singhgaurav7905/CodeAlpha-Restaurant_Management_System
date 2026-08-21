package com.restaurant.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * A raw stock item tracked in the storeroom (e.g. "Mozzarella Cheese", 5 kg).
 * Quantities auto-decrement as orders are confirmed and can be topped up
 * via the restock API.
 */
@Entity
@Table(name = "inventory_items")
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    /** Unit of measure, e.g. kg, litre, piece. */
    @NotBlank
    private String unit;

    @NotNull
    @DecimalMin(value = "0.0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantityInStock;

    /** The stock level below which this ingredient is flagged as "low stock". */
    @NotNull
    @DecimalMin(value = "0.0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal reorderThreshold;

    /** Quantity restocked to by default when replenished. Used purely for reporting/suggestions. */
    @Column(precision = 10, scale = 2)
    private BigDecimal parLevel;

    public InventoryItem() {}

    public InventoryItem(String name, String unit, BigDecimal quantityInStock, BigDecimal reorderThreshold, BigDecimal parLevel) {
        this.name = name;
        this.unit = unit;
        this.quantityInStock = quantityInStock;
        this.reorderThreshold = reorderThreshold;
        this.parLevel = parLevel;
    }

    public boolean isLowStock() {
        return quantityInStock != null && reorderThreshold != null
                && quantityInStock.compareTo(reorderThreshold) <= 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getQuantityInStock() { return quantityInStock; }
    public void setQuantityInStock(BigDecimal quantityInStock) { this.quantityInStock = quantityInStock; }

    public BigDecimal getReorderThreshold() { return reorderThreshold; }
    public void setReorderThreshold(BigDecimal reorderThreshold) { this.reorderThreshold = reorderThreshold; }

    public BigDecimal getParLevel() { return parLevel; }
    public void setParLevel(BigDecimal parLevel) { this.parLevel = parLevel; }
}
