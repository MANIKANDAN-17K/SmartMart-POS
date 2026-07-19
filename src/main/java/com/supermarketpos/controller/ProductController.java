package com.supermarketpos.controller;

import com.supermarketpos.model.Category;
import com.supermarketpos.model.Product;
import com.supermarketpos.model.Role;
import com.supermarketpos.model.User;
import com.supermarketpos.service.AuthService;
import com.supermarketpos.service.CategoryService;
import com.supermarketpos.service.ProductService;
import com.supermarketpos.session.UserSession;
import com.supermarketpos.util.AlertUtil;
import com.supermarketpos.util.BarcodeUtil;
import com.supermarketpos.util.CurrencyUtil;
import com.supermarketpos.util.DateUtil;
import com.supermarketpos.util.ImageUtil;
import com.supermarketpos.util.ValidationUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ProductController {

    private static final Logger LOGGER = LogManager.getLogger(ProductController.class);

    // Header & User Profile
    @FXML private Label loggedUserLabel;
    @FXML private Label roleLabel;
    @FXML private Label dateLabel;
    @FXML private Label currentTimeLabel;

    // KPI Cards
    @FXML private Label lblTotalProducts;
    @FXML private Label lblActiveProducts;
    @FXML private Label lblLowStockProducts;

    // Form controls
    @FXML private TextField nameField;
    @FXML private TextField barcodeField;
    @FXML private TextField skuField;
    @FXML private ComboBox<Category> categoryDropdown;
    @FXML private TextField costPriceField;
    @FXML private TextField sellingPriceField;
    @FXML private TextField gstField;
    @FXML private ImageView previewImage;
    @FXML private TextField searchField;

    // Table
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, String> barcodeColumn;
    @FXML private TableColumn<Product, String> skuColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    @FXML private TableColumn<Product, BigDecimal> sellingPriceColumn;
    @FXML private TableColumn<Product, String> statusColumn;

    // Buttons
    @FXML private Button saveButton;
    @FXML private Button updateButton;
    @FXML private Button clearButton;
    @FXML private Button uploadImageButton;
    @FXML private Button activateButton;
    @FXML private Button deactivateButton;
    @FXML private Button refreshButton;
    @FXML private Button logoutButton;

    // Sidebar navigation buttons
    @FXML private Button btnDashboardNav;
    @FXML private Button btnNewBillNav;
    @FXML private Button btnProductsNav;
    @FXML private Button btnInventoryNav;
    @FXML private Button btnPurchasesNav;
    @FXML private Button btnCustomersNav;
    @FXML private Button btnSuppliersNav;
    @FXML private Button btnReportsNav;
    @FXML private Button btnSettingsNav;
    @FXML private Button btnUsersNav;
    @FXML private Button btnBackupNav;

    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();
    private final AuthService authService = new AuthService();

    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private Product selectedProduct;
    private String pendingImagePath;

    private Timeline clockTimeline;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadUserInfo();
        startClock();

        productTable.setItems(productList);
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedProduct = newVal;
            populateForm(newVal);
        });

        loadCategories();
        loadProducts();
        LOGGER.info("ProductController initialized");
    }

    private void setupTableColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        skuColumn.setCellValueFactory(new PropertyValueFactory<>("sku"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        sellingPriceColumn.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
        sellingPriceColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : CurrencyUtil.format(val));
            }
        });

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusLabel"));
        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(status);
                    badge.getStyleClass().add("badge");
                    if ("Active".equalsIgnoreCase(status)) {
                        badge.getStyleClass().add("badge-active");
                    } else {
                        badge.getStyleClass().add("badge-inactive");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
    }

    private void loadUserInfo() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (loggedUserLabel != null) loggedUserLabel.setText(currentUser.getUsername());
            if (roleLabel != null) roleLabel.setText(UserSession.getInstance().getCurrentRole().name());
        } else {
            if (loggedUserLabel != null) loggedUserLabel.setText("admin");
            if (roleLabel != null) roleLabel.setText("Administrator");
        }
    }

    private void startClock() {
        updateDateAndTimeLabels();
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateDateAndTimeLabels()));
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();
    }

    private void updateDateAndTimeLabels() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd MMM yyyy EEEE");
        if (dateLabel != null) dateLabel.setText(now.format(dateFmt));
        if (currentTimeLabel != null) currentTimeLabel.setText(DateUtil.nowDisplay());
    }

    private void updateKpis() {
        int total = productList.size();
        long active = productList.stream().filter(Product::isActive).count();

        if (lblTotalProducts != null) lblTotalProducts.setText(String.valueOf(total));
        if (lblActiveProducts != null) lblActiveProducts.setText(String.valueOf(active));
        if (lblLowStockProducts != null) lblLowStockProducts.setText("2");
    }

    @FXML
    private void handleSearch() {
        try {
            productList.setAll(productService.searchProduct(searchField.getText()));
            updateKpis();
        } catch (SQLException e) {
            LOGGER.error("Search failed", e);
            AlertUtil.showError("Database Error", "Unable to search products: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        if (searchField != null) searchField.clear();
        clearForm();
        loadProducts();
    }

    @FXML
    private void handleGenerateBarcode() {
        barcodeField.setText(BarcodeUtil.generateBarcode());
    }

    @FXML
    private void handleUploadImage() {
        Window window = uploadImageButton.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(window);
        if (file != null) {
            try {
                String savedPath = ImageUtil.storeProductImage(file.toPath());
                pendingImagePath = savedPath;
                previewImage.setImage(new Image(file.toURI().toString()));
            } catch (java.io.IOException e) {
                LOGGER.error("Failed to upload image", e);
                AlertUtil.showError("Image Error", "Unable to save image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSave() {
        try {
            Category selectedCategory = categoryDropdown.getValue();
            Integer categoryId = selectedCategory != null ? selectedCategory.getId() : null;

            productService.createProduct(
                    nameField.getText(),
                    barcodeField.getText(),
                    skuField.getText(),
                    categoryId,
                    parseDecimalOrNull(costPriceField.getText()),
                    parseDecimalOrNull(sellingPriceField.getText()),
                    parseDecimalOrNull(gstField.getText()),
                    pendingImagePath
            );

            AlertUtil.showInfo("Success", "Product created successfully.");
            clearForm();
            loadProducts();
        } catch (ProductService.ValidationException e) {
            AlertUtil.showWarning("Validation Error", e.getMessage());
        } catch (SQLException e) {
            LOGGER.error("Failed to save product", e);
            AlertUtil.showError("Database Error", "Unable to create product: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedProduct == null) {
            AlertUtil.showWarning("No Selection", "Please select a product to update.");
            return;
        }

        try {
            Category selectedCategory = categoryDropdown.getValue();
            Integer categoryId = selectedCategory != null ? selectedCategory.getId() : null;

            String imageToSave = pendingImagePath != null ? pendingImagePath : selectedProduct.getImagePath();

            productService.updateProduct(
                    selectedProduct.getId(),
                    nameField.getText(),
                    barcodeField.getText(),
                    skuField.getText(),
                    categoryId,
                    parseDecimalOrNull(costPriceField.getText()),
                    parseDecimalOrNull(sellingPriceField.getText()),
                    parseDecimalOrNull(gstField.getText()),
                    imageToSave
            );

            AlertUtil.showInfo("Success", "Product updated successfully.");
            clearForm();
            loadProducts();
        } catch (ProductService.ValidationException e) {
            AlertUtil.showWarning("Validation Error", e.getMessage());
        } catch (SQLException e) {
            LOGGER.error("Failed to update product", e);
            AlertUtil.showError("Database Error", "Unable to update product: " + e.getMessage());
        }
    }

    @FXML
    private void handleActivate() {
        if (selectedProduct == null) {
            AlertUtil.showWarning("No Selection", "Please select a product to activate.");
            return;
        }
        try {
            productService.activateProduct(selectedProduct.getId());
            loadProducts();
        } catch (SQLException e) {
            LOGGER.error("Failed to activate product", e);
            AlertUtil.showError("Database Error", "Unable to activate product: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeactivate() {
        if (selectedProduct == null) {
            AlertUtil.showWarning("No Selection", "Please select a product to deactivate.");
            return;
        }
        try {
            productService.deactivateProduct(selectedProduct.getId());
            loadProducts();
        } catch (SQLException e) {
            LOGGER.error("Failed to deactivate product", e);
            AlertUtil.showError("Database Error", "Unable to deactivate product: " + e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
    }

    private void loadCategories() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            categoryDropdown.setItems(FXCollections.observableArrayList(categories));
        } catch (SQLException e) {
            LOGGER.error("Failed to load categories", e);
            AlertUtil.showError("Database Error", "Unable to load categories: " + e.getMessage());
        }
    }

    private void loadProducts() {
        try {
            productList.setAll(productService.getAllProducts());
            updateKpis();
        } catch (SQLException e) {
            LOGGER.error("Failed to load products", e);
            AlertUtil.showError("Database Error", "Unable to load products: " + e.getMessage());
        }
    }

    private void populateForm(Product product) {
        if (product == null) {
            clearForm();
            return;
        }

        nameField.setText(product.getName());
        barcodeField.setText(product.getBarcode());
        skuField.setText(product.getSku());
        costPriceField.setText(product.getCostPrice() != null ? product.getCostPrice().toString() : "");
        sellingPriceField.setText(product.getSellingPrice() != null ? product.getSellingPrice().toString() : "");
        gstField.setText(product.getGstPercent() != null ? product.getGstPercent().toString() : "");

        if (product.getCategoryId() > 0) {
            categoryDropdown.getItems().stream()
                    .filter(c -> c.getId() == product.getCategoryId())
                    .findFirst()
                    .ifPresent(categoryDropdown::setValue);
        } else {
            categoryDropdown.setValue(null);
        }

        if (product.getImagePath() != null && !product.getImagePath().isBlank()) {
            File imageFile = new File(product.getImagePath());
            if (imageFile.exists()) {
                previewImage.setImage(new Image(imageFile.toURI().toString()));
            } else {
                previewImage.setImage(loadDefaultImage());
            }
        } else {
            previewImage.setImage(loadDefaultImage());
        }
    }

    private void clearForm() {
        nameField.clear();
        barcodeField.clear();
        skuField.clear();
        costPriceField.clear();
        sellingPriceField.clear();
        gstField.clear();
        categoryDropdown.setValue(null);
        previewImage.setImage(loadDefaultImage());
        pendingImagePath = null;
        selectedProduct = null;
        productTable.getSelectionModel().clearSelection();
    }

    private Image loadDefaultImage() {
        var stream = ImageUtil.loadDefaultImageStream();
        return stream != null ? new Image(stream) : null;
    }

    private BigDecimal parseDecimalOrNull(String value) {
        if (ValidationUtil.isNullOrEmpty(value)) {
            return null;
        }
        return new BigDecimal(value.trim());
    }

    // Navigation handlers
    private void stopTimers() {
        if (clockTimeline != null) clockTimeline.stop();
    }

    private void navigateTo(String fxmlPath, String title, Button sourceButton) {
        try {
            stopTimers();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) sourceButton.getScene().getWindow();
            double w = stage.getWidth();
            double h = stage.getHeight();
            stage.setScene(new javafx.scene.Scene(root, w, h));
            stage.setTitle(title);
        } catch (Exception e) {
            LOGGER.error("Navigation error to " + title, e);
            AlertUtil.showError("Navigation Error", "Could not load " + title + " view: " + e.getMessage());
        }
    }

    @FXML private void onDashboard() { navigateTo("/fxml/dashboard.fxml", "Dashboard", btnProductsNav); }
    @FXML private void onNewBill() { navigateTo("/fxml/billing.fxml", "Billing", btnProductsNav); }
    @FXML private void onProducts() { handleRefresh(); }
    @FXML private void onInventory() { navigateTo("/fxml/inventory.fxml", "Inventory", btnProductsNav); }
    @FXML private void onPurchases() { navigateTo("/fxml/purchase.fxml", "Purchases", btnProductsNav); }
    @FXML private void onCustomersNav() { navigateTo("/fxml/customer.fxml", "Customers", btnProductsNav); }
    @FXML private void onSuppliersNav() { navigateTo("/fxml/supplier.fxml", "Suppliers", btnProductsNav); }
    @FXML private void onReports() { navigateTo("/fxml/report.fxml", "Reports", btnProductsNav); }
    @FXML private void onSettings() {
        if (UserSession.getInstance().getCurrentRole() != Role.ADMIN) {
            AlertUtil.showError("Access Denied", "Only ADMIN users can access Settings.");
            return;
        }
        navigateTo("/fxml/settings.fxml", "Settings", btnProductsNav);
    }
    @FXML private void onUsersNav() {
        if (UserSession.getInstance().getCurrentRole() != Role.ADMIN) {
            AlertUtil.showError("Access Denied", "Only ADMIN users can manage Users.");
            return;
        }
        navigateTo("/fxml/user_management.fxml", "Users Management", btnProductsNav);
    }
    @FXML private void onBackupNav() { AlertUtil.showInfo("Database Backup", "Initiating database backup process..."); }

    @FXML
    private void onLogout() {
        boolean confirmed = AlertUtil.showConfirm("Confirm Logout", "Are you sure you want to log out?");
        if (!confirmed) return;
        stopTimers();
        authService.logout();
        navigateTo("/fxml/login.fxml", "Login", logoutButton);
    }
}