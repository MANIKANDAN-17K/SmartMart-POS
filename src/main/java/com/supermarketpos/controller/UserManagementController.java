package com.supermarketpos.controller;

import com.supermarketpos.model.Role;
import com.supermarketpos.model.User;
import com.supermarketpos.service.UserService;
import com.supermarketpos.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class UserManagementController {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, Role> roleColumn;
    @FXML private TableColumn<User, Boolean> activeColumn;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<Role> roleComboBox;

    @FXML private Button createButton;
    @FXML private Button updateButton;
    @FXML private Button deactivateButton;

    private final UserService userService = new UserService();
    private final ObservableList<User> users = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));

        roleComboBox.setItems(FXCollections.observableArrayList(Role.values()));
        userTable.setItems(users);

        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldUser, newUser) -> {
            if (newUser != null) {
                usernameField.setText(newUser.getUsername());
                roleComboBox.setValue(newUser.getRole());
                passwordField.clear();
            }
        });

        refreshUsers();
    }

    private void refreshUsers() {
        users.setAll(userService.getAllUsers());
    }

    @FXML
    private void onCreate() {
        try {
            userService.createUser(usernameField.getText(), passwordField.getText(), roleComboBox.getValue());
            AlertUtil.showInfo("Success", "User created successfully");
            clearForm();
            refreshUsers();
        } catch (IllegalArgumentException e) {
            AlertUtil.showError("Create Failed", e.getMessage());
        }
    }

    @FXML
    private void onUpdate() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Select a user to update");
            return;
        }
        try {
            userService.updateUser(selected.getId(), usernameField.getText(), roleComboBox.getValue());
            AlertUtil.showInfo("Success", "User updated successfully");
            clearForm();
            refreshUsers();
        } catch (IllegalArgumentException e) {
            AlertUtil.showError("Update Failed", e.getMessage());
        }
    }

    @FXML
    private void onDeactivate() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Select a user to deactivate");
            return;
        }
        boolean confirmed = AlertUtil.showConfirm("Confirm Deactivation",
                "Deactivate user \"" + selected.getUsername() + "\"?");
        if (confirmed) {
            userService.deactivateUser(selected.getId());
            AlertUtil.showInfo("Success", "User deactivated");
            clearForm();
            refreshUsers();
        }
    }

    private void clearForm() {
        usernameField.clear();
        passwordField.clear();
        roleComboBox.setValue(null);
        userTable.getSelectionModel().clearSelection();
    }
}
