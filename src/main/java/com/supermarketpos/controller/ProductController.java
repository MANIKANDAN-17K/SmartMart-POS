package com.supermarketpos.controller;

import com.supermarketpos.model.Category;
import com.supermarketpos.model.Product;
import com.supermarketpos.service.CategoryService;
import com.supermarketpos.service.ProductService;
import com.supermarketpos.util.AlertUtil;
import com.supermarketpos.util.BarcodeUtil;
import com.supermarketpos.util.ImageUtil;
import com.supermarketpos.util.ValidationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

public class ProductController {

    @FXML private TextField nameField;
    @FXML private TextField barcodeField;
    @FXML private TextField skuField;
    @FXML private ComboBox<Category> categoryDropdown;
    @FXML private TextField costPriceField;
    @FXML private TextField sellingPriceField;
    @FXML private TextField gstField;
    @FXML private ImageView previewImage;
    @FXML private TextField searchField;

    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, String> barcodeColumn;
    @FXML private TableColumn<Product, String> skuColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    @FXML private TableColumn<Product, BigDecimal> sellingPriceColumn;
    @FXML private TableColumn<Product, String> statusColumn;

    @FXML private Button saveButton;
    @FXML private Button updateButton;
    @FXML private Button clearButton;
    @FXML private Button uploadImageButton;
    @FXML private Button activateButton;
    @FXML private Button deactivateButton;

    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();

    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private Product selectedProduct;
    private String pendingImagePath;

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        skuColumn.setCellValueFactory(new PropertyValueFactory<>("sku"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        sellingPriceColumn.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusLabel"));

        productTable.setItems(productList);
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedProduct = newVal;
            populateForm(newVal);
        });

        loadCategoryDropdown();
        loadProducts();
    }

    @FXML
    private void handleSearch() {
        try {
            productList.setAll(productService.searchProduct(searchField.getText()));
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Unable to search products: " + e.getMessage());
        }
    }

    @FXML
    private void handleFilterByCategory() {
        Category selected = categoryDropdown.getValue();
        if (selected == null) {
            loadProducts();
            return;
        }
        try {
            productList.setAll(productService.getProductsByCategory(selected.getId()));
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Unable to filter products: " + e.getMessage());
        }
    }

    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        Window window = uploadImageButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(window);
        if (file == null) return;

        try {
            String storedPath = ImageUtil.storeProductImage(file.toPath());
            pendingImagePath = storedPath;
            previewImage.setImage(new Image(file.toURI().toString()));
        } catch (IOException e) {
            AlertUtil.showError("Image Upload Failed", e.getMessage());
        }
    }

    @FXML
    private void handleGenerateBarcode() {
        barcodeField.setText(BarcodeUtil.generateBarcode());
    }

    @FXML
    private void handleSave() {
        try {
            BigDecimal cost = parseDecimalOrNull(costPriceField.getText());
            BigDecimal selling = parseDecimalOrNull(sellingPriceField.getText());
            BigDecimal gst = parseDecimalOrNull(gstField.getText());
            Category category = categoryDropdown.getValue();
            int categoryId = category != null ? category.getId() : -1;

            productService.createProduct(
                    nameField.getText(), barcodeField.getText(), skuField.getText(),
                    categoryId, cost, selling, gst, pendingImagePath);

            AlertUtil.showInfo("Success", "Product created successfully.");
            clearForm();
            loadProducts();
        } catch (ProductService.ValidationException e) {
            AlertUtil.showWarning("Validation Error", e.getMessage());
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Unable to create product: " + e.getMessage());
        } catch (NumberFormatException e) {
            AlertUtil.showWarning("Validation Error", "Cost price, selling price and GST must be valid numbers.");
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedProduct == null) {
            AlertUtil.showWarning("No Selection", "Please select a product to update.");
            return;
        }
        try {
            BigDecimal cost = parseDecimalOrNull(costPriceField.getText());
            BigDecimal selling = parseDecimalOrNull(sellingPriceField.getText());
            BigDecimal gst = parseDecimalOrNull(gstField.getText());
            Category category = categoryDropdown.getValue();
            int categoryId = category != null ? category.getId() : -1;

            String imagePath = pendingImagePath != null ? pendingImagePath : selectedProduct.getImagePath();

            productService.updateProduct(
                    selectedProduct.getId(), nameField.getText(), barcodeField.getText(), skuField.getText(),
                    categoryId, cost, selling, gst, imagePath);

            AlertUtil.showInfo("Success", "Product updated successfully.");
            clearForm();
            loadProducts();
        } catch (ProductService.ValidationException e) {
            AlertUtil.showWarning("Validation Error", e.getMessage());
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Unable to update product: " + e.getMessage());
        } catch (NumberFormatException e) {
            AlertUtil.showWarning("Validation Error", "Cost price, selling price and GST must be valid numbers.");
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
            AlertUtil.showError("Database Error", "Unable to activate product: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeactivate() {
        if (selectedProduct == null) {
            AlertUtil.showWarning("No Selection", "Please select a product to deactivate.");
            return;
        }
        boolean confirmed = AlertUtil.showConfirmation("Confirm Deactivation",
                "Inactive products will no longer appear in Billing. Continue?");
        if (!confirmed) return;

        try {
            productService.deactivateProduct(selectedProduct.getId());
            loadProducts();
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Unable to deactivate product: " + e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
    }

    private void loadCategoryDropdown() {
        try {
            List<Category> categories = categoryService.getAllActiveCategories();
            categoryDropdown.setItems(FXCollections.observableArrayList(categories));
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Unable to load categories: " + e.getMessage());
        }
    }

    private void loadProducts() {
        try {
            productList.setAll(productService.getAllProducts());
        } catch (SQLException e) {
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
        costPriceField.setText(product.getCostPrice().toPlainString());
        sellingPriceField.setText(product.getSellingPrice().toPlainString());
        gstField.setText(product.getGstPercentage().toPlainString());

        for (Category c : categoryDropdown.getItems()) {
            if (c.getId() == product.getCategoryId()) {
                categoryDropdown.setValue(c);
                break;
            }
        }

        pendingImagePath = null;
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
}