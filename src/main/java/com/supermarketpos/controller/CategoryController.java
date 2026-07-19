package com.supermarketpos.controller;

import com.supermarketpos.model.Category;
import com.supermarketpos.service.CategoryService;
import com.supermarketpos.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;

public class CategoryController {

    @FXML private TextField searchField;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;

    @FXML private TableView<Category> categoryTable;
    @FXML private TableColumn<Category, String> nameColumn;
    @FXML private TableColumn<Category, String> descriptionColumn;
    @FXML private TableColumn<Category, String> statusColumn;

    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button activateButton;
    @FXML private Button deactivateButton;
    @FXML private Button refreshButton;

    private final CategoryService categoryService = new CategoryService();
    private final ObservableList<Category> categoryList = FXCollections.observableArrayList();
    private Category selectedCategory;

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusLabel"));

        categoryTable.setItems(categoryList);
        categoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedCategory = newVal;
            populateForm(newVal);
        });

        loadCategories();
    }

    @FXML
    private void handleSearch() {
        try {
            categoryList.setAll(categoryService.searchCategory(searchField.getText()));
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Unable to search categories: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        try {
            categoryService.createCategory(nameField.getText(), descriptionField.getText());
            AlertUtil.showInfo("Success", "Category created successfully.");
            clearForm();
            loadCategories();
        } catch (CategoryService.ValidationException e) {
            AlertUtil.showWarning("Validation Error", e.getMessage());
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Unable to create category: " + e.getMessage());
        }
    }

    @FXML
    private void handleEdit() {
        if (selectedCategory == null) {
            AlertUtil.showWarning("No Selection", "Please select a category to edit.");
            return;
        }
        try {
            categoryService.updateCategory(selectedCategory.getId(), nameField.getText(), descriptionField.getText());
            AlertUtil.showInfo("Success", "Category updated successfully.");
            clearForm();
            loadCategories();
        } catch (CategoryService.ValidationException e) {
            AlertUtil.showWarning("Validation Error", e.getMessage());
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Unable to update category: " + e.getMessage());
        }
    }

    @FXML
    private void handleActivate() {
        if (selectedCategory == null) {
            AlertUtil.showWarning("No Selection", "Please select a category to activate.");
            return;
        }
        try {
            categoryService.activateCategory(selectedCategory.getId());
            loadCategories();
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Unable to activate category: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeactivate() {
        if (selectedCategory == null) {
            AlertUtil.showWarning("No Selection", "Please select a category to deactivate.");
            return;
        }
        boolean confirmed = AlertUtil.showConfirmation("Confirm Deactivation",
                "Inactive categories cannot receive new products. Continue?");
        if (!confirmed) return;

        try {
            categoryService.deactivateCategory(selectedCategory.getId());
            loadCategories();
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Unable to deactivate category: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        clearForm();
        loadCategories();
    }

    private void loadCategories() {
        try {
            categoryList.setAll(categoryService.getAllCategories());
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Unable to load categories: " + e.getMessage());
            return;
        }
    }

    private void populateForm(Category category) {
        if (category == null) {
            clearForm();
            return;
        }
        nameField.setText(category.getName());
        descriptionField.setText(category.getDescription());
    }

    private void clearForm() {
        nameField.clear();
        descriptionField.clear();
        selectedCategory = null;
        categoryTable.getSelectionModel().clearSelection();
    }
}