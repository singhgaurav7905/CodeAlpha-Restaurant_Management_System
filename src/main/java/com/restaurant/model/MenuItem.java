package com.restaurant.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * A single item that can be ordered from the menu, e.g. "Margherita Pizza".
 * Linked to the {@link InventoryItem}s it consumes so stock can be
 * auto-deducted whenever an order containing this item is placed.
 */
@Entity
@Table(name = "menu_items")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Size(max = 500)
    private String description;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MenuCategory category;

    private boolean vegetarian;

    /** Whether the item is currently sellable (chef can 86 an item without deleting it). */
    private boolean available = true;

    private String imageUrl;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "menu_item_ingredients",
        joinColumns = @JoinColumn(name = "menu_item_id"),
        inverseJoinColumns = @JoinColumn(name = "inventory_item_id")
    )
    @JsonIgnore
    private Set<InventoryItem> ingredients = new HashSet<>();

    public MenuItem() {}

    public MenuItem(String name, String description, BigDecimal price, MenuCategory category, boolean vegetarian) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.vegetarian = vegetarian;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public MenuCategory getCategory() { return category; }
    public void setCategory(MenuCategory category) { this.category = category; }

    public boolean isVegetarian() { return vegetarian; }
    public void setVegetarian(boolean vegetarian) { this.vegetarian = vegetarian; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Set<InventoryItem> getIngredients() { return ingredients; }
    public void setIngredients(Set<InventoryItem> ingredients) { this.ingredients = ingredients; }
}
