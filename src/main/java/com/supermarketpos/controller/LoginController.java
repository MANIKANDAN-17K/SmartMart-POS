package com.supermarketpos.controller;

import com.supermarketpos.model.Role;
import com.supermarketpos.service.AuthService;
import com.supermarketpos.util.AlertUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private CheckBox showPasswordCheckBox;
    @FXML private Button loginButton;
    @FXML private Button exitButton;

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

        AuthService.AuthResult result = authService.login(username, password);
        if (!result.isSuccess()) {
            AlertUtil.showError("Login Failed", result.getMessage());
            return;
        }

        AlertUtil.showInfo("Welcome", "Logged in as " + result.getUser().getUsername()
                + " (" + result.getUser().getRole() + ")");
        openNextScreenForRole(result.getUser().getRole());
    }

    @FXML
    private void onExit(ActionEvent event) {
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
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
            stage.setScene(new javafx.scene.Scene(dashboardRoot));
            stage.setTitle("Dashboard");
        } catch (java.io.IOException e) {
            AlertUtil.showError("Navigation Error", "Could not load the dashboard.");
        }
    }
}
