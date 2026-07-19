package com.supermarketpos.controller;

import com.supermarketpos.model.Role;
import com.supermarketpos.service.AuthService;
import com.supermarketpos.util.AlertUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoginController {

    private static final Logger LOGGER = LogManager.getLogger(LoginController.class);

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private CheckBox showPasswordCheckBox;
    @FXML private Button loginButton;
    @FXML private Button exitButton;
    @FXML private Hyperlink forgotPasswordLink;

    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        // visiblePasswordField mirrors passwordField's text so "Show Password"
        // can reveal the plain text without a separate model
        visiblePasswordField.setManaged(false);
        visiblePasswordField.setVisible(false);
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());

        showPasswordCheckBox.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            passwordField.setVisible(!isSelected);
            passwordField.setManaged(!isSelected);
            visiblePasswordField.setVisible(isSelected);
            visiblePasswordField.setManaged(isSelected);
        });
    }

    @FXML
    private void onLogin(ActionEvent event) {
        String username = usernameField.getText() != null ? usernameField.getText().trim() : "";
        String password = passwordField.getText();
        if (password == null || password.isEmpty()) {
            password = visiblePasswordField.getText();
        }

        AuthService.AuthResult result = authService.login(username, password);
        if (!result.isSuccess()) {
            AlertUtil.showError("Login Failed", result.getMessage());
            return;
        }

        openNextScreenForRole(result.getUser().getRole());
    }

    @FXML
    private void onExit(ActionEvent event) {
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onForgotPassword(ActionEvent event) {
        String currentUsername = usernameField.getText() != null ? usernameField.getText().trim() : "";
        javafx.scene.control.TextInputDialog userDialog = new javafx.scene.control.TextInputDialog(currentUsername);
        userDialog.setTitle("Forgot Password");
        userDialog.setHeaderText("Account Verification");
        userDialog.setContentText("Enter your username:");

        java.util.Optional<String> userResult = userDialog.showAndWait();
        if (userResult.isEmpty() || userResult.get().isBlank()) {
            return;
        }

        String username = userResult.get().trim();
        javafx.scene.control.TextInputDialog passDialog = new javafx.scene.control.TextInputDialog();
        passDialog.setTitle("Reset Password");
        passDialog.setHeaderText("Set new password for user: " + username);
        passDialog.setContentText("Enter your new password:");

        java.util.Optional<String> passResult = passDialog.showAndWait();
        if (passResult.isEmpty() || passResult.get().isBlank()) {
            return;
        }

        String newPassword = passResult.get().trim();
        AuthService.AuthResult result = authService.resetPassword(username, newPassword);
        if (result.isSuccess()) {
            AlertUtil.showInfo("Success", "Password for user '" + username + "' has been reset successfully.");
            usernameField.setText(username);
            passwordField.setText(newPassword);
        } else {
            AlertUtil.showError("Reset Failed", result.getMessage());
        }
    }

    /**
     * Integration point for later sprints: MainApp/DashboardController own
     * scene switching and are outside this sprint's allowed files, so this
     * is intentionally left as a hook rather than a hard navigation call.
     */
    private void openNextScreenForRole(Role role) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/dashboard.fxml"));
            javafx.scene.Parent dashboardRoot = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            double w = stage.getWidth();
            double h = stage.getHeight();
            stage.setScene(new javafx.scene.Scene(dashboardRoot, w, h));
            stage.setTitle("Dashboard");
        } catch (Exception e) {
            LOGGER.error("Navigation error to dashboard", e);
            AlertUtil.showError("Navigation Error", "Could not load Dashboard: " + e.getMessage());
        }
    }
}
