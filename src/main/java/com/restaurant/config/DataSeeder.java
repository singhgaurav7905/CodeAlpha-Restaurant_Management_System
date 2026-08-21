package com.restaurant.config;

import com.restaurant.model.*;
import com.restaurant.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * Populates the database with sample menu/table/inventory data and a
 * default staff login the first time the app runs against an empty
 * database. Safe to leave in place permanently - both seed methods check
 * whether data already exists before inserting anything.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final MenuItemRepository menuItemRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final RestaurantTableRepository tableRepository;
    private final StaffUserRepository staffUserRepository;
    private final PasswordEncoder passwordEncoder;
        private final String adminUsername;
        private final String adminPassword;

    public DataSeeder(MenuItemRepository menuItemRepository,
                       InventoryItemRepository inventoryItemRepository,
                       RestaurantTableRepository tableRepository,
                       StaffUserRepository staffUserRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${app.seed.admin.username}") String adminUsername,
                       @Value("${app.seed.admin.password}") String adminPassword) {
        this.menuItemRepository = menuItemRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.tableRepository = tableRepository;
        this.staffUserRepository = staffUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        seedStaffUsers();
        if (menuItemRepository.count() > 0) return; // menu/tables/inventory already seeded

        // ---- Inventory ----
        InventoryItem flour = inventoryItemRepository.save(new InventoryItem("Pizza Dough", "piece", bd(40), bd(10), bd(60)));
        InventoryItem mozzarella = inventoryItemRepository.save(new InventoryItem("Mozzarella Cheese", "kg", bd(8), bd(2), bd(15)));
        InventoryItem tomato = inventoryItemRepository.save(new InventoryItem("Tomato Sauce", "l", bd(6), bd(2), bd(10)));
        InventoryItem basil = inventoryItemRepository.save(new InventoryItem("Fresh Basil", "kg", bd(1.5), bd(0.5), bd(3)));
        InventoryItem pasta = inventoryItemRepository.save(new InventoryItem("Spaghetti", "kg", bd(12), bd(3), bd(20)));
        InventoryItem cream = inventoryItemRepository.save(new InventoryItem("Fresh Cream", "l", bd(5), bd(2), bd(10)));
        InventoryItem chicken = inventoryItemRepository.save(new InventoryItem("Chicken Breast", "kg", bd(10), bd(3), bd(20)));
        InventoryItem lettuce = inventoryItemRepository.save(new InventoryItem("Romaine Lettuce", "kg", bd(4), bd(1), bd(8)));
        InventoryItem salmon = inventoryItemRepository.save(new InventoryItem("Salmon Fillet", "kg", bd(3), bd(2), bd(10)));
        InventoryItem potato = inventoryItemRepository.save(new InventoryItem("Potatoes", "kg", bd(15), bd(4), bd(25)));
        InventoryItem chocolate = inventoryItemRepository.save(new InventoryItem("Dark Chocolate", "kg", bd(2), bd(1), bd(5)));
        InventoryItem coffeeBeans = inventoryItemRepository.save(new InventoryItem("Coffee Beans", "kg", bd(4), bd(1), bd(8)));
        InventoryItem lemon = inventoryItemRepository.save(new InventoryItem("Lemons", "piece", bd(30), bd(10), bd(50)));
        InventoryItem shrimp = inventoryItemRepository.save(new InventoryItem("Shrimp", "kg", bd(1.2), bd(1), bd(6)));

        // ---- Menu items (with linked ingredients so orders auto-deduct stock) ----
        seedItem("Margherita Pizza", "San Marzano tomato, fresh mozzarella, basil, wood-fired crust.",
                "12.50", MenuCategory.PIZZA, true, set(flour, mozzarella, tomato, basil));
        seedItem("Pepperoni Pizza", "Classic pepperoni, mozzarella, house tomato sauce.",
                "13.90", MenuCategory.PIZZA, false, set(flour, mozzarella, tomato));
        seedItem("Spaghetti Carbonara", "Guanciale, egg yolk, pecorino, cracked black pepper.",
                "14.50", MenuCategory.PASTA, false, set(pasta, cream));
        seedItem("Spaghetti Aglio e Olio", "Garlic, chili flakes, olive oil, parsley, parmesan.",
                "11.90", MenuCategory.PASTA, true, set(pasta));
        seedItem("Grilled Chicken Caesar Salad", "Romaine, grilled chicken, parmesan, house Caesar dressing.",
                "13.00", MenuCategory.SALAD, false, set(lettuce, chicken));
        seedItem("Pan-Seared Salmon", "Crisp-skin salmon, lemon butter sauce, seasonal greens.",
                "22.00", MenuCategory.MAIN_COURSE, false, set(salmon, lemon));
        seedItem("Herb Roasted Chicken", "Half chicken, garlic herb jus, roasted potatoes.",
                "18.50", MenuCategory.MAIN_COURSE, false, set(chicken, potato));
        seedItem("Garlic Butter Shrimp", "Sauteed shrimp, garlic butter, chili, crusty bread.",
                "16.00", MenuCategory.STARTER, false, set(shrimp, lemon));
        seedItem("Truffle Fries", "Hand-cut fries, truffle oil, parmesan, herbs.",
                "8.50", MenuCategory.STARTER, true, set(potato));
        seedItem("Tomato Basil Soup", "Slow-roasted tomato, fresh basil, cream swirl.",
                "7.50", MenuCategory.SOUP, true, set(tomato, basil, cream));
        seedItem("Molten Chocolate Lava Cake", "Warm dark chocolate cake, molten center, vanilla ice cream.",
                "9.00", MenuCategory.DESSERT, true, set(chocolate));
        seedItem("Classic Tiramisu", "Espresso-soaked ladyfingers, mascarpone, cocoa dust.",
                "8.50", MenuCategory.DESSERT, true, set(coffeeBeans, cream));
        seedItem("Fresh Lemonade", "Hand-squeezed lemons, mint, soda.",
                "4.50", MenuCategory.BEVERAGE, true, set(lemon));
        seedItem("Espresso", "Double shot, single origin.",
                "3.50", MenuCategory.BEVERAGE, true, set(coffeeBeans));
        seedItem("Chef's Tasting Special", "Three-course chef's choice, changes weekly.",
                "35.00", MenuCategory.SPECIAL, false, set());

        // ---- Tables ----
        tableRepository.save(new RestaurantTable("T1", 2, "Main Hall"));
        tableRepository.save(new RestaurantTable("T2", 2, "Main Hall"));
        tableRepository.save(new RestaurantTable("T3", 4, "Main Hall"));
        tableRepository.save(new RestaurantTable("T4", 4, "Main Hall"));
        tableRepository.save(new RestaurantTable("T5", 6, "Main Hall"));
        tableRepository.save(new RestaurantTable("P1", 2, "Patio"));
        tableRepository.save(new RestaurantTable("P2", 4, "Patio"));
        tableRepository.save(new RestaurantTable("R1", 8, "Rooftop"));
        tableRepository.save(new RestaurantTable("R2", 4, "Rooftop"));
        tableRepository.save(new RestaurantTable("B1", 2, "Bar"));
    }

    private void seedItem(String name, String desc, String price, MenuCategory cat, boolean veg, Set<InventoryItem> ingredients) {
        MenuItem item = new MenuItem(name, desc, new BigDecimal(price), cat, veg);
        item.setIngredients(ingredients);
        menuItemRepository.save(item);
    }

    private Set<InventoryItem> set(InventoryItem... items) {
        return new HashSet<>(java.util.Arrays.asList(items));
    }

    private BigDecimal bd(double v) {
        return BigDecimal.valueOf(v);
    }

    private void seedStaffUsers() {
        if (staffUserRepository.count() > 0) return;
                StaffUser admin = new StaffUser(adminUsername, passwordEncoder.encode(adminPassword), "Restaurant Admin", Role.ADMIN);
        staffUserRepository.save(admin);
                System.out.println("Seeded initial staff account for username: " + adminUsername);
    }
}
