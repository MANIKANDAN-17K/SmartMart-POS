package com.supermarketpos.controller;

import com.supermarketpos.model.Role;
import com.supermarketpos.model.User;
import com.supermarketpos.service.AuthService;
import com.supermarketpos.service.UserService;
import com.supermarketpos.session.UserSession;
import com.supermarketpos.util.AlertUtil;
import com.supermarketpos.util.DateUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class UserManagementController {

    private static final Logger LOGGER = LogManager.getLogger(UserManagementController.class);

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, Role> roleColumn;
    @FXML private TableColumn<User, Boolean> activeColumn;

    @FXML private TextField searchField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<Role> roleComboBox;

    @FXML private Button createButton;
    @FXML private Button updateButton;
    @FXML private Button deactivateButton;
    @FXML private Button btnActivate;
    @FXML private Button refreshButton;
    @FXML private Button logoutButton;

    // Header & User Profile
    @FXML private Label loggedUserLabel;
    @FXML private Label roleLabel;
    @FXML private Label dateLabel;
    @FXML private Label currentTimeLabel;

    // KPI Cards
    @FXML private Label lblTotalUsers;
    @FXML private Label lblAdminCount;
    @FXML private Label lblCashierCount;

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

    private final UserService userService = new UserService();
    private final AuthService authService = new AuthService();
    private final ObservableList<User> users = FXCollections.observableArrayList();

    private Timeline clockTimeline;

    @FXML
    private void initialize() {
        setupTableColumns();
        loadUserInfo();
        startClock();

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
        LOGGER.info("UserManagementController initialized");
    }

    private void setupTableColumns() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Role role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(role.name());
                    badge.getStyleClass().add("badge");
                    if (role == Role.ADMIN) {
                        badge.getStyleClass().add("badge-admin");
                    } else {
                        badge.getStyleClass().add("badge-cashier");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                if (empty || active == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(active ? "ACTIVE" : "INACTIVE");
                    badge.getStyleClass().add("badge");
                    if (active) {
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

    private void refreshUsers() {
        users.setAll(userService.getAllUsers());
        updateKpis();
    }

    private void updateKpis() {
        int total = users.size();
        long admins = users.stream().filter(u -> u.getRole() == Role.ADMIN).count();
        long cashiers = users.stream().filter(u -> u.getRole() == Role.CASHIER).count();

        if (lblTotalUsers != null) lblTotalUsers.setText(String.valueOf(total));
        if (lblAdminCount != null) lblAdminCount.setText(String.valueOf(admins));
        if (lblCashierCount != null) lblCashierCount.setText(String.valueOf(cashiers));
    }

    @FXML
    private void onSearch() {
        if (searchField == null || searchField.getText().trim().isEmpty()) {
            refreshUsers();
            return;
        }
        String q = searchField.getText().trim().toLowerCase();
        users.setAll(userService.getAllUsers().stream()
                .filter(u -> u.getUsername().toLowerCase().contains(q))
                .toList());
        updateKpis();
    }

    @FXML
    private void onRefresh() {
        if (searchField != null) searchField.clear();
        clearForm();
        refreshUsers();
    }

    @FXML
    private void onCreate() {
        try {
            userService.createUser(usernameField.getText(), passwordField.getText(), roleComboBox.getValue());
            AlertUtil.showInfo("Success", "User account created successfully");
            clearForm();
            refreshUsers();
        } catch (IllegalArgumentException e) {
            AlertUtil.showError("Create Failed", e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Failed to create user", e);
            AlertUtil.showError("Error", "Could not create user: " + e.getMessage());
        }
    }

    @FXML
    private void onUpdate() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Select a user to update role");
            return;
        }
        try {
            userService.updateUser(selected.getId(), usernameField.getText(), roleComboBox.getValue());
            AlertUtil.showInfo("Success", "User role updated successfully");
            clearForm();
            refreshUsers();
        } catch (IllegalArgumentException e) {
            AlertUtil.showError("Update Failed", e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Failed to update user role", e);
            AlertUtil.showError("Error", "Could not update user: " + e.getMessage());
        }
    }

    @FXML
    private void onActivate() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Select a user account to activate");
            return;
        }
        try {
            userService.activateUser(selected.getId());
            AlertUtil.showInfo("Success", "User account activated");
            clearForm();
            refreshUsers();
        } catch (Exception e) {
            LOGGER.error("Failed to activate user", e);
            AlertUtil.showError("Error", "Could not activate user: " + e.getMessage());
        }
    }

    @FXML
    private void onDeactivate() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Select a user account to deactivate");
            return;
        }
        boolean confirmed = AlertUtil.showConfirm("Confirm Deactivation",
                "Deactivate user account \"" + selected.getUsername() + "\"?");
        if (confirmed) {
            try {
                userService.deactivateUser(selected.getId());
                AlertUtil.showInfo("Success", "User account deactivated");
                clearForm();
                refreshUsers();
            } catch (Exception e) {
                LOGGER.error("Failed to deactivate user", e);
                AlertUtil.showError("Error", "Could not deactivate user: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onResetPassword() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Select a user account to reset password");
            return;
        }
        String newPass = passwordField.getText().trim();
        if (newPass.isEmpty()) {
            AlertUtil.showWarning("Password Required", "Type a new password in the Password field.");
            return;
        }
        try {
            authService.resetPassword(selected.getUsername(), newPass);
            AlertUtil.showInfo("Success", "Password updated for user: " + selected.getUsername());
            passwordField.clear();
        } catch (Exception e) {
            LOGGER.error("Failed to reset password", e);
            AlertUtil.showError("Error", "Could not reset password: " + e.getMessage());
        }
    }

    @FXML
    private void onClear() {
        clearForm();
    }

    private void clearForm() {
        usernameField.clear();
        passwordField.clear();
        roleComboBox.setValue(null);
        userTable.getSelectionModel().clearSelection();
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

    @FXML private void onDashboard() { navigateTo("/fxml/dashboard.fxml", "Dashboard", btnUsersNav); }
    @FXML private void onNewBill() { navigateTo("/fxml/billing.fxml", "Billing", btnUsersNav); }
    @FXML private void onProducts() { navigateTo("/fxml/product.fxml", "Products", btnUsersNav); }
    @FXML private void onInventory() { navigateTo("/fxml/inventory.fxml", "Inventory", btnUsersNav); }
    @FXML private void onPurchases() { navigateTo("/fxml/purchase.fxml", "Purchases", btnUsersNav); }
    @FXML private void onCustomersNav() { navigateTo("/fxml/customer.fxml", "Customers", btnUsersNav); }
    @FXML private void onSuppliersNav() { navigateTo("/fxml/supplier.fxml", "Suppliers", btnUsersNav); }
    @FXML private void onReports() { navigateTo("/fxml/report.fxml", "Reports", btnUsersNav); }
    @FXML private void onSettings() {
        if (UserSession.getInstance().getCurrentRole() != Role.ADMIN) {
            AlertUtil.showError("Access Denied", "Only ADMIN users can access Settings.");
            return;
        }
        navigateTo("/fxml/settings.fxml", "Settings", btnUsersNav);
    }
    @FXML private void onUsersNav() { onRefresh(); }
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
