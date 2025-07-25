package com.lilach.client.controllers;

import com.lilach.client.models.UserDTO;
import com.lilach.client.services.ApiService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;

public class LoginController extends BaseController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    
    @FXML
    public void initialize() {
        loginButton.setGraphic(new FontIcon("fas-sign-in-alt"));
        registerButton.setGraphic(new FontIcon("fas-user-plus"));
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Validation Error", "Please enter both username and password");
            return;
        }
        
        try {
            UserDTO user = ApiService.login(username, password);
            if (user != null) {
                redirectBasedOnRole(user);
            } else {
                showAlert("Login Failed", "Invalid credentials");
            }
        } catch (IOException e) {
            showAlert("Connection Error", "Failed to connect to server: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleRegister() {
        navigateTo("/com/lilach/client/views/register.fxml", "Create Account");
    }
    
    private void redirectBasedOnRole(UserDTO user) {
        try {
 
            switch (user.getRole()) {
                case "STORE_MANAGER":
                
                    navigateTo("/com/lilach/client/views/store_manager_dashboard.fxml", "Store Manager Dashboard");
                    break;
                case "NETWORK_ADMIN":
                    navigateTo("/com/lilach/client/views/network_admin_dashboard.fxml", "Network Admin Dashboard");
                    break;
                case "CUSTOMER":
                default:
                                        
                    navigateTo("/com/lilach/client/views/catalog.fxml", "Lilach Flower Shop Catalog");
                    break;
            }
        } catch (Exception e) {
            showAlert("Navigation Error", "Failed to load view: " + e.getMessage());
        }
    }
    
   
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}