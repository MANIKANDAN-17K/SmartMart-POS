# SupermarketPOS — Sprint Plan (v1.0)

> Each sprint is handed to a separate AI model with a focused prompt.
> All sprints build on the same locked directory structure.
> Never rename or move files between sprints.

---

## Sprint 1 — Project Foundation & Database Layer
**Covers:** Module 1 (Project Setup)
**Files touched:**
- `pom.xml`
- `config.properties`
- `application.properties`
- `log4j2.xml`
- `MainApp.java`, `SplashScreen.java`, `SplashController.java`
- `config/AppConfig.java`, `DatabaseConfig.java`, `SessionConfig.java`
- `database/DatabaseInitializer.java`, `ConnectionPool.java`, `MigrationRunner.java`
- `sql/schema.sql`, `sql/seed_data.sql`
- `sql/migrations/V1__initial_schema.sql`, `V2__add_gst_fields.sql`
- `fxml/splash.fxml`
- `css/common.css`, `light-theme.css`, `dark-theme.css`
- `scripts/run.sh`, `run.bat`

**Goal:** Running Maven project. Double-click JAR → splash screen → MySQL auto-initialized → login window opens.

---

## Sprint 2 — Authentication & Session
**Covers:** Module 2 (Authentication)
**Files touched:**
- `model/User.java`, `Role.java`
- `dao/UserDao.java`, `BaseDao.java`
- `service/AuthService.java`, `UserService.java`
- `session/UserSession.java`
- `controller/LoginController.java`, `UserManagementController.java`, `ChangePasswordController.java`
- `fxml/login.fxml`, `user_management.fxml`, `change_password.fxml`
- `util/HashUtil.java`, `AlertUtil.java`, `ValidationUtil.java`

**Goal:** Login with Admin/Cashier roles. Session persists. Change password. Admin can create/edit/deactivate users.

---

## Sprint 3 — Dashboard
**Covers:** Module 3 (Dashboard)
**Files touched:**
- `model/StoreSettings.java`
- `dao/SettingsDao.java`, `BillDao.java` (partial), `ProductDao.java` (partial)
- `service/SettingsService.java`
- `controller/DashboardController.java`
- `fxml/dashboard.fxml`
- `css/dashboard.css`
- `util/CurrencyUtil.java`, `DateUtil.java`

**Goal:** Dashboard shows today's sales, profit, total products, low stock count, last 5 bills, quick action buttons.

---

## Sprint 4 — Product & Category Management
**Covers:** Module 4 (Product Management)
**Files touched:**
- `model/Category.java`, `Product.java`
- `dao/CategoryDao.java`, `ProductDao.java`
- `service/CategoryService.java`, `ProductService.java`
- `controller/CategoryController.java`, `ProductController.java`
- `fxml/category.fxml`, `product.fxml`
- `util/BarcodeUtil.java`, `ImageUtil.java`
- `images/default-product.png`

**Goal:** Full CRUD for categories and products. Barcode, SKU, cost price, selling price, GST %, image upload, active/inactive toggle, search.

---

## Sprint 5 — Supplier Management
**Covers:** Module 5 (Supplier Management)
**Files touched:**
- `model/Supplier.java`
- `dao/SupplierDao.java`
- `service/SupplierService.java`
- `controller/SupplierController.java`
- `fxml/supplier.fxml`

**Goal:** Add/edit/delete suppliers with contact details. View purchase history per supplier.

---

## Sprint 6 — Purchase Management
**Covers:** Module 6 (Purchase Management)
**Files touched:**
- `model/Purchase.java`, `PurchaseItem.java`
- `dao/PurchaseDao.java`, `PurchaseItemDao.java`
- `service/PurchaseService.java`
- `controller/PurchaseController.java`
- `fxml/purchase.fxml`
- `templates/invoice_template.html`
- `util/InvoiceNumberUtil.java`

**Goal:** Create purchase entry against supplier. Generate purchase invoice. Receiving stock auto-updates inventory.

---

## Sprint 7 — Inventory Management
**Covers:** Module 7 (Inventory)
**Files touched:**
- `model/StockMovement.java`, `StockAdjustment.java`
- `dao/StockMovementDao.java`, `StockAdjustmentDao.java`
- `service/InventoryService.java`
- `event/StockUpdateEvent.java`
- `controller/InventoryController.java`
- `fxml/inventory.fxml`

**Goal:** View current stock levels, low stock alerts, manual stock adjustment, full movement history (purchase in / sale out / adjustment).

---

## Sprint 8 — Billing (Core Module)
**Covers:** Module 8 (Billing)
**Files touched:**
- `model/Bill.java`, `BillItem.java`
- `dao/BillDao.java`, `BillItemDao.java`
- `service/BillingService.java`, `ReceiptService.java`
- `event/BillCreatedEvent.java`
- `controller/BillingController.java`
- `fxml/billing.fxml`
- `css/billing.css`
- `templates/receipt_template.html`
- `util/PrintUtil.java`

**Goal:** Full POS billing screen. Barcode scan / product search → cart → quantity/discount → GST calc → Cash/Card/UPI/Split payment → print receipt → auto stock deduction. Cancel bill (admin only). Reprint.

---

## Sprint 9 — Customer Management
**Covers:** Module 9 (Customer)
**Files touched:**
- `model/Customer.java`
- `dao/CustomerDao.java`
- `service/CustomerService.java`
- `controller/CustomerController.java`
- `fxml/customer.fxml`

**Goal:** Add/edit/delete customers. Phone number search. View full purchase history per customer.

---

## Sprint 10 — Reports
**Covers:** Module 10 (Reports)
**Files touched:**
- `report/DailySalesReport.java`
- `report/MonthlySalesReport.java`
- `report/ProductSalesReport.java`
- `report/PurchaseReport.java`
- `report/StockReport.java`
- `report/ProfitReport.java`
- `service/ReportService.java`
- `controller/ReportController.java`
- `fxml/report.fxml`
- `util/ExcelExportUtil.java`

**Goal:** All 6 reports with date range filters, on-screen tables, and export to Excel.

---

## Sprint 11 — Settings, Audit Log & Backup
**Covers:** Module 11 (Settings)
**Files touched:**
- `model/AuditLog.java`, `TaxSetting.java`, `StoreSettings.java`
- `dao/AuditLogDao.java`, `SettingsDao.java`
- `service/AuditService.java`, `BackupService.java`, `SettingsService.java`
- `controller/SettingsController.java`
- `fxml/settings.fxml`
- `util/ThemeUtil.java`
- `scripts/backup.sh`

**Goal:** Store details, tax config, receipt header/footer, light/dark theme toggle, database backup to file, restore from file, audit log viewer.

---

## Sprint 12 — Google Sheets Sync
**Covers:** Module 12 (Google Sheets Sync ⭐)
**Files touched:**
- `google/GoogleAuthHandler.java`
- `google/SheetsApiClient.java`
- `google/SheetsSyncMapper.java`
- `model/SyncLog.java`
- `dao/SyncLogDao.java`
- `service/GoogleSheetsService.java`, `SyncService.java`
- `event/SyncEvent.java`
- `controller/GoogleSheetsController.java`
- `fxml/google_sheets.fxml`

**Goal:** OAuth2 Google login, spreadsheet picker, manual sync (Products/Sales/Purchases/Customers), sync log, last sync timestamp. App works 100% offline — sync is optional.

---

## Sprint 13 — Final Polish & Executable JAR
**Covers:** Cross-cutting, packaging
**Files touched:**
- `pom.xml` (maven-shade-plugin / javafx-maven-plugin final config)
- `scripts/install.sh`, `run.sh`, `run.bat`
- `README.md`
- `docs/user-manual.md`
- All test files in `src/test/`

**Goal:** Single executable fat JAR. Startup < 5 seconds. All error paths handled with proper rollback. Smoke test all modules end to end.

---

## Future Sprints (v1.1 / v1.2 / v2.0)
Placeholder packages already created under `future/`:
- `future/email/` — Email receipts
- `future/qr/` — QR receipts
- `future/analytics/` — Business dashboard & health score
- `future/multiterminal/` — Multi-terminal billing
- `future/multistore/` — Multi-store support
- `future/mobile/` — Mobile companion app
- `future/ai/` — AI forecasting
- `future/loyalty/` — Customer loyalty program
