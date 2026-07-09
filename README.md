# 🛒 SmartMart POS

> **SmartMart POS** is an **offline-first supermarket Point of Sale (POS)** application built with **Java 21** and **JavaFX**. It is designed for supermarkets and retail stores to manage billing, inventory, purchases, suppliers, customers, and reports through a modern desktop interface.

---

## ✨ Features

* 🔐 Secure role-based authentication
* 🛍️ Fast POS billing system
* 📦 Product & Inventory Management
* 🚚 Supplier & Purchase Management
* 👥 Customer Management
* 📊 Sales and Profit Reports
* 🖨️ Receipt Printing
* 📷 Barcode Support
* ☁️ Optional Google Sheets Synchronization
* 💾 Automatic Database Backup
* ⚡ Offline-first architecture
* 🎨 Light & Dark Theme Support

---

# 🛠️ Technology Stack

| Layer              | Technology        |
| ------------------ | ----------------- |
| Language           | Java 21 LTS       |
| UI                 | JavaFX 21         |
| Database           | MySQL 8.0         |
| Build Tool         | Maven             |
| Database Migration | Flyway            |
| Connection Pool    | HikariCP          |
| Logging            | Log4j2            |
| Excel Export       | Apache POI        |
| Barcode            | ZXing             |
| Testing            | JUnit 5 + Mockito |

---

# 📋 Prerequisites

Install the following before running the application:

* Java 21 LTS
* MySQL 8.0+
* Maven 3.9+ (only required to build from source)
* Git (optional)

---

# 🚀 Installation

## 1. Clone the Repository

```bash
git clone https://github.com/MANIKANDAN-17K/SmartMart-POS.git

cd SmartMart-POS
```

---

## 2. Create the Database

```sql
CREATE DATABASE smartmart_pos
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

CREATE USER 'smartmart_user'@'localhost'
IDENTIFIED BY 'your_secure_password';

GRANT ALL PRIVILEGES
ON smartmart_pos.*
TO 'smartmart_user'@'localhost';

FLUSH PRIVILEGES;
```

---

## 3. Configure the Application

Copy the sample configuration.

```bash
cp config.properties.example config.properties
```

Update the following values.

```properties
db.host=localhost
db.port=3306
db.name=smartmart_pos
db.username=smartmart_user
db.password=your_secure_password

app.name=SmartMart POS
app.version=1.0.0

store.name=My Supermarket
store.phone=+91 00000 00000
store.address=123 Main Street

inventory.low_stock_threshold=10

backup.directory=./backups
```

> **Note:** `config.properties` stays outside the executable JAR so you can change store details or database credentials without rebuilding the application.

---

## 4. Build

```bash
mvn clean package
```

The executable JAR will be generated inside:

```text
target/
└── SmartMart-POS-1.0.jar
```

---

## 5. Run

### Windows

```bat
scripts\run.bat
```

### Linux / macOS

```bash
chmod +x scripts/run.sh
./scripts/run.sh
```

Or run directly:

```bash
java -jar target/SmartMart-POS-1.0.jar
```

On first launch, Flyway automatically creates all required database tables.

---

# 🔑 Default Login

| Role    | Username | Password   |
| ------- | -------- | ---------- |
| Admin   | admin    | admin123   |
| Cashier | cashier  | cashier123 |

> Change the default passwords after the first login.

---

# 📂 Project Structure

```text
SmartMart-POS
│
├── src
│   ├── main
│   │   ├── java
│   │   └── resources
│   └── test
│
├── docs
├── scripts
├── config.properties.example
├── pom.xml
└── README.md
```

---

# 📦 Modules

| Module               | Status    |
| -------------------- | --------- |
| Project Setup        | ⏳ Planned |
| Authentication       | ⏳ Planned |
| Dashboard            | ⏳ Planned |
| Product Management   | ⏳ Planned |
| Supplier Management  | ⏳ Planned |
| Purchase Management  | ⏳ Planned |
| Inventory Management | ⏳ Planned |
| Billing              | ⏳ Planned |
| Customer Management  | ⏳ Planned |
| Reports              | ⏳ Planned |
| Settings             | ⏳ Planned |
| Google Sheets Sync   | ⏳ Planned |
| Final Packaging      | ⏳ Planned |

---

# 💡 Architecture

SmartMart POS follows an **offline-first** architecture.

* Local MySQL database
* No internet required for billing
* Automatic transaction rollback
* Role-based access control
* Flyway database migrations
* External configuration
* Single executable JAR deployment

---

# ⌨️ Keyboard Shortcuts

| Key   | Action         |
| ----- | -------------- |
| F1    | Barcode Input  |
| F2    | Product Search |
| F3    | Discount       |
| F4    | Payment        |
| F5    | Print Receipt  |
| ESC   | Cancel         |
| Enter | Confirm        |

---

# ☁️ Google Sheets Sync

The application can optionally synchronize:

* Products
* Sales
* Purchases
* Customers
* Expenses

Synchronization is **one-way**:

```
SmartMart POS
      │
      ▼
Google Sheets
```

The application never depends on an internet connection.

---

# 📜 Scripts

| Script     | Purpose                         |
| ---------- | ------------------------------- |
| run.sh     | Start application (Linux/macOS) |
| run.bat    | Start application (Windows)     |
| backup.sh  | Backup database                 |
| install.sh | First-time installation         |

---

# 🗺️ Roadmap

### Version 1.1

* Email Receipts
* QR Code Receipts
* Barcode Label Printing

### Version 1.2

* Business Dashboard
* Daily Closing
* Purchase Suggestions

### Version 2.0

* Multi-Terminal Support
* Multi-Store Support
* Mobile Companion App
* AI Sales Forecasting
* Loyalty Program

---

# 🤝 Contributing

1. Fork the repository.
2. Create a new feature branch.

```bash
git checkout -b feature/my-feature
```

3. Commit your changes.

```bash
git commit -m "Add my feature"
```

4. Push your branch.

```bash
git push origin feature/my-feature
```

5. Open a Pull Request.

---

# 📄 License

This project is licensed under the **MIT License**.

See the `LICENSE` file for details.
