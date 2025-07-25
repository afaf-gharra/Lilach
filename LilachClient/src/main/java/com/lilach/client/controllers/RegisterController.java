package com.lilach.client.controllers;

import com.lilach.client.models.UserDTO;
import com.lilach.client.services.ApiService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController extends BaseController  {
    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField creditCardField;
    @FXML private CheckBox termsCheckbox;
    @FXML private Button registerButton;
    @FXML private Hyperlink loginLink;
    
    @FXML
    public void initialize() {
        registerButton.setOnAction(e -> handleRegister());
        loginLink.setOnAction(e -> navigateToLogin());
    }
    
    private void handleRegister() {
        if (!validateForm()) {
            return;
        }
        
        UserDTO newUser = new UserDTO();
        newUser.setFullName(fullNameField.getText());
        newUser.setUsername(usernameField.getText());
        newUser.setPassword(passwordField.getText());
        newUser.setEmail(emailField.getText());
        newUser.setPhone(phoneField.getText());
        newUser.setCreditCard(creditCardField.getText());
        newUser.setAccountType("MEMBER");
        newUser.setRole("CUSTOMER"); // Assuming role is set to USER for regular users
        
        
        try {
            UserDTO createdUser = ApiService.register(newUser);
            if (createdUser != null) {
                showSuccess("Account Created", "Your account has been created successfully!");
                navigateToWithSize("/com/lilach/client/views/catalog.fxml", "Lilach Flower Shop Catalog", 1200, 800);
            } else {
                showError("Registration Failed", "Failed to create account. Please try again.");
            }
        } catch (IOException e) {
            showError("Connection Error", "Failed to connect to server: " + e.getMessage());
        }
    }
    
    private boolean validateForm() {
        if (fullNameField.getText().isEmpty()) {
            showError("Validation Error", "Full name is required");
            return false;
        }
        if (usernameField.getText().isEmpty()) {
            showError("Validation Error", "Username is required");
            return false;
        }
        if (passwordField.getText().isEmpty()) {
            showError("Validation Error", "Password is required");
            return false;
        }
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("Validation Error", "Passwords do not match");
            return false;
        }
      
        if (!termsCheckbox.isSelected()) {
            showError("Validation Error", "You must agree to terms and conditions");
            return false;
        }
        return true;
    }
    
    private void navigateToLogin() {
        try {
            Stage stage = (Stage) loginLink.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/com/lilach/client/views/login.fxml"));
            stage.setScene(new Scene(root, 800, 600));
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Navigation Error", "Failed to load login view: " + e.getMessage());
        }
    }

}