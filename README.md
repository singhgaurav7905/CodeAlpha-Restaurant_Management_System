# Ember & Row — Restaurant Management System

A full-stack restaurant management system: a Java (Spring Boot) backend with a
REST API for orders, tables, reservations, inventory, and reporting, plus a
styled customer-facing website and an admin dashboard.

## Stack

- **Backend:** Java 17, Spring Boot 3.3 (Web, Data JPA, Validation)
- **Database:** PostgreSQL by default (data persists across restarts) — an
  H2 in-memory fallback is included for quick zero-setup testing
- **Frontend:** Static HTML/CSS/JS served by Spring Boot (no build step),
  under `src/main/resources/static`

## Project layout

```
src/main/java/com/restaurant/
  model/        JPA entities (MenuItem, Order, OrderItem, RestaurantTable,
                Reservation, InventoryItem) + enums
  repository/   Spring Data JPA repositories
  service/      Business logic — order processing, table availability,
                inventory auto-deduction, reservation overlap checks, reports
  controller/   REST controllers (/api/...)
  dto/          Request/response payloads
  exception/    Custom exceptions + a global @RestControllerAdvice handler
  config/       DataSeeder — populates sample menu/tables/inventory on boot

src/main/resources/
  application.properties
  static/
    index.html       customer site: menu, live floor plan, ordering, reservations
    admin.html        staff dashboard: orders, tables, reservations, inventory, menu, reports
    css/style.css      shared design system
    css/admin.css      dashboard layout
    js/app.js          customer site logic
    js/admin.js        admin dashboard logic
```

## Running it

Requires JDK 17+, Maven 3.9+, and a running PostgreSQL server.

### 1. Create the database

```bash
createdb restaurant_db
# or, via psql:
psql -U postgres -c "CREATE DATABASE restaurant_db;"
```

### 2. Configure environment variables

Copy `.env.example` to `.env`, then set the values in your shell or IDE run
configuration. Do not commit `.env`.

```text
DB_URL=jdbc:postgresql://localhost:5432/restaurant_db
DB_USERNAME=postgres
DB_PASSWORD=your-database-password
ADMIN_USERNAME=restaurant-admin
ADMIN_PASSWORD=your-initial-admin-password
```

On Windows PowerShell, set them for the current terminal with:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/restaurant_db"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "your-database-password"
$env:ADMIN_USERNAME = "restaurant-admin"
$env:ADMIN_PASSWORD = "your-initial-admin-password"
```

The initial staff account is created only when `staff_users` is empty. Change
its password from the admin dashboard after the first login.

No manual schema work is needed — `spring.jpa.hibernate.ddl-auto=update`
creates and updates all tables from the JPA entities on startup.

### 3. Run it

```bash
mvn spring-boot:run
```

Then open:

- **Customer site:** http://localhost:8080/
- **Admin dashboard:** http://localhost:8080/admin.html

Sample menu items, tables, and inventory are seeded automatically on first
startup (see `DataSeeder` — it only seeds if the `menu_items` table is
empty, so it won't duplicate data on later restarts). Because Postgres is a
real persistent database, **orders, reservations, and inventory changes now
survive restarts.**

### Using H2 instead (quick, no Postgres install needed)

In `application.properties`, comment out the Postgres block and uncomment
the H2 block at the bottom of the file. Note that H2 here runs in-memory, so
data resets on every restart — use it for quick local testing, not for
anything you want to keep. You can browse it live at
http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:restaurant_db`,
user `sa`, no password) while the app is running.

## Core business logic

- **Order processing** (`OrderService.placeOrder`): validates every line item
  is available and has enough stock *before* touching anything, then creates
  the order, deducts inventory, and occupies the dine-in table — so a bad
  line item never leaves the system half-updated.
- **Table availability** (`ReservationService.isTableFreeForSlot`): a table
  is free for a requested window only if no other active reservation
  overlaps it. Reservations without a requested table auto-assign the
  smallest table that fits the party and is free.
- **Inventory auto-update** (`InventoryService`): stock is deducted per
  ingredient when an order is placed, and `isLowStock()` flags anything at
  or below its reorder threshold for the stock-alerts report.
- **Order status state machine** (`OrderService.updateStatus`): once an order
  is `COMPLETED` or `CANCELLED` it can't be changed further; finishing or
  cancelling a dine-in order automatically flips its table to `CLEANING`.

## API reference

All responses are wrapped as `{ success, message, data, timestamp }`.

| Method | Path | Description |
|---|---|---|
| GET | `/api/menu?category=&availableOnly=` | Browse the menu |
| POST | `/api/menu` | Create a menu item |
| PUT | `/api/menu/{id}` | Update a menu item |
| PATCH | `/api/menu/{id}/availability?available=` | 86 / restore a dish |
| DELETE | `/api/menu/{id}` | Delete a menu item |
| GET | `/api/tables?availableOnly=` | List tables |
| POST | `/api/tables` | Add a table |
| PATCH | `/api/tables/{id}/status?status=` | Change table status |
| GET | `/api/reservations` | List reservations |
| POST | `/api/reservations` | Book a table (auto-assigns if `tableId` omitted) |
| POST | `/api/reservations/{id}/seat` \| `/cancel` \| `/complete` | Reservation lifecycle |
| GET | `/api/orders?status=&activeOnly=` | List orders |
| POST | `/api/orders` | Place an order (validates stock, deducts inventory) |
| PATCH | `/api/orders/{id}/status?status=` | Advance order status |
| POST | `/api/orders/{id}/cancel` | Cancel an order |
| GET | `/api/inventory?lowStockOnly=` | List stock |
| POST | `/api/inventory` | Add an ingredient |
| PATCH | `/api/inventory/{id}/restock` \| `/adjust` | Add stock / set stock level |
| GET | `/api/reports/daily-sales?date=YYYY-MM-DD` | Daily sales report |
| GET | `/api/reports/stock-alerts` | Ingredients at/below reorder threshold |

## Notes

- No authentication is included — `admin.html` is reachable by anyone who
  knows the URL. Add Spring Security if you need to gate it for production.
- Recipes are simplified: each menu item consumes 1 unit of each linked
  ingredient per item ordered. Adjust `InventoryService` if you need
  per-recipe quantities.
