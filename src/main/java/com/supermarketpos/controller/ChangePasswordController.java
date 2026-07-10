package com.supermarketpos.controller;

import com.supermarketpos.service.AuthService;
import com.supermarketpos.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;

public class ChangePasswordController {

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final AuthService authService = new AuthService();

    @FXML
    private void onSave() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (newPassword == null || !newPassword.equals(confirmPassword)) {
            AlertUtil.showError("Change Password Failed", "New password and confirmation do not match");
            return;
        }

        AuthService.AuthResult result = authService.changePassword(currentPassword, newPassword);
        if (!result.isSuccess()) {
            AlertUtil.showError("Change Password Failed", result.getMessage());
            return;
        }

        AlertUtil.showInfo("Success", "Password changed successfully");
        clearForm();
    }

    @FXML
    private void onCancel() {
        clearForm();
    }

    private void clearForm() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }
}
