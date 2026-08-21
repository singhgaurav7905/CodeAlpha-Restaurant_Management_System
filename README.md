# Ember & Row — Restaurant Management System

A full-stack restaurant management system built with **Java and Spring Boot**, featuring a REST API and a modern web interface for managing menus, orders, tables, reservations, inventory, and reports.

## ✨ Features

* 🍽️ Customer-facing restaurant website
* 📋 Menu management
* 🛒 Order management
* 🪑 Restaurant table management
* 📅 Table reservations
* 📦 Inventory management
* 📊 Sales and stock reports
* 🔄 Automatic inventory deduction
* 🖥️ Admin dashboard
* 💾 PostgreSQL persistence
* 🗄️ H2 fallback for quick testing

## 🛠️ Tech Stack

| Layer       | Technology                    |
| ----------- | ----------------------------- |
| Backend     | Java 17, Spring Boot 3.3      |
| API         | Spring Web, REST              |
| Persistence | Spring Data JPA, Hibernate    |
| Database    | PostgreSQL                    |
| Testing DB  | H2                            |
| Frontend    | HTML, CSS, Vanilla JavaScript |
| Build Tool  | Maven                         |

## 📁 Project Structure

```text
src/
├── main/
│   ├── java/com/restaurant/
│   │   ├── model/          # JPA entities and enums
│   │   ├── repository/     # Spring Data JPA repositories
│   │   ├── service/        # Business logic
│   │   ├── controller/     # REST API controllers
│   │   ├── dto/            # Request/response DTOs
│   │   ├── exception/      # Custom exceptions & error handling
│   │   └── config/         # Application configuration & data seeder
│   │
│   └── resources/
│       ├── application.properties
│       └── static/
│           ├── index.html
│           ├── admin.html
│           ├── css/
│           │   ├── style.css
│           │   └── admin.css
│           └── js/
│               ├── app.js
│               └── admin.js
│
├── test/
└── ...
├── .env.example
├── .gitignore
├── pom.xml
└── README.md
```

## 🚀 Getting Started

### Prerequisites

Make sure you have installed:

* JDK 17+
* Maven 3.9+
* PostgreSQL
* Git

Check your installations:

```bash
java -version
mvn -version
psql --version
```

### 1. Clone the Repository

```bash
git clone <your-repository-url>
cd Ember-and-Row
```

### 2. Create the Database

Using `createdb`:

```bash
createdb restaurant_db
```

Or using `psql`:

```bash
psql -U postgres -c "CREATE DATABASE restaurant_db;"
```

No manual table creation is required. Hibernate creates/updates the database tables from the JPA entities.

### 3. Configure Environment Variables

Create a `.env` file using `.env.example` as a reference.

```text
DB_URL=jdbc:postgresql://localhost:5432/restaurant_db
DB_USERNAME=postgres
DB_PASSWORD=your-database-password
ADMIN_USERNAME=restaurant-admin
ADMIN_PASSWORD=your-initial-admin-password
```

**Do not commit ****`.env`**** to Git.**

For Windows PowerShell:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/restaurant_db"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "your-database-password"
$env:ADMIN_USERNAME = "restaurant-admin"
$env:ADMIN_PASSWORD = "your-initial-admin-password"
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

The application will start on:

```text
http://localhost:8080
```

### 🌐 Application

**Customer Website**

```text
http://localhost:8080
```

**Admin Dashboard**

```text
http://localhost:8080/admin.html
```

Sample menu items, tables, and inventory are seeded automatically when required.

Because PostgreSQL is persistent, orders, reservations, inventory changes, and other database records survive application restarts.

## 🔌 REST API

All API endpoints use the `/api` prefix.

| Method | Endpoint                          | Description          |
| ------ | --------------------------------- | -------------------- |
| GET    | `/api/menu`                       | Browse menu          |
| POST   | `/api/menu`                       | Create menu item     |
| PUT    | `/api/menu/{id}`                  | Update menu item     |
| PATCH  | `/api/menu/{id}/availability`     | Enable/disable item  |
| DELETE | `/api/menu/{id}`                  | Delete menu item     |
| GET    | `/api/tables`                     | List tables          |
| POST   | `/api/tables`                     | Add table            |
| PATCH  | `/api/tables/{id}/status`         | Update table status  |
| GET    | `/api/reservations`               | List reservations    |
| POST   | `/api/reservations`               | Create reservation   |
| POST   | `/api/reservations/{id}/seat`     | Seat reservation     |
| POST   | `/api/reservations/{id}/cancel`   | Cancel reservation   |
| POST   | `/api/reservations/{id}/complete` | Complete reservation |
| GET    | `/api/orders`                     | List orders          |
| POST   | `/api/orders`                     | Place order          |
| PATCH  | `/api/orders/{id}/status`         | Update order status  |
| POST   | `/api/orders/{id}/cancel`         | Cancel order         |
| GET    | `/api/inventory`                  | List inventory       |
| POST   | `/api/inventory`                  | Add inventory item   |
| PATCH  | `/api/inventory/{id}/restock`     | Restock inventory    |
| PATCH  | `/api/inventory/{id}/adjust`      | Adjust stock         |
| GET    | `/api/reports/daily-sales`        | Daily sales report   |
| GET    | `/api/reports/stock-alerts`       | Low-stock report     |

API responses follow a common structure:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {},
  "timestamp": "2026-08-21T10:00:00"
}
```

## 🧠 Core Business Logic

### Order Processing

Before placing an order, the system validates menu availability and inventory. Once validation succeeds, the order is created and inventory is automatically deducted.

### Table & Reservation Management

Reservations check for overlapping bookings. When a table isn't explicitly selected, the system can automatically assign the smallest suitable available table.

### Inventory

Inventory is automatically reduced when orders are placed. Ingredients at or below their reorder threshold are reported as low stock.

### Order Status

Completed and cancelled orders cannot be modified further. Dine-in tables are automatically moved to `CLEANING` when their order is completed or cancelled.

## 🗄️ Database

PostgreSQL is the default database.

Hibernate is configured to automatically create/update tables:

```properties
spring.jpa.hibernate.ddl-auto=update
```

An H2 in-memory database is also available for quick zero-setup testing.

## 🔐 Security Note

The current version does **not** include complete authentication and authorization for the admin dashboard.

For production use, consider adding:

* Spring Security
* Role-based access control
* Secure password management
* JWT/session authentication
* HTTPS
* Proper secrets management

The initial admin credentials should be changed after the first login.

## 🧪 Testing

Run the test suite with:

```bash
mvn test
```

Build the project with:

```bash
mvn clean package
```

Run the generated JAR:

```bash
java -jar target/<generated-jar-name>.jar
```

## 🔮 Future Improvements

* [ ] Spring Security & role-based authentication
* [ ] Customer accounts
* [ ] Online payment integration
* [ ] Real-time order tracking
* [ ] Kitchen dashboard
* [ ] Advanced analytics
* [ ] Email/SMS notifications
* [ ] Detailed recipe-based inventory
* [ ] Multiple restaurant branches
* [x] Automated database migrations

## 👨‍💻 Author

**Gaurav Singh**

Built with Java, Spring Boot, PostgreSQL, and Vanilla JavaScript.

---

⭐ If you find this project useful, consider giving it a star.
