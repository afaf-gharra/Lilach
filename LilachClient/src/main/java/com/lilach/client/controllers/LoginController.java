package com.lilach.client.controllers;

import com.lilach.client.models.UserDTO;
import com.lilach.client.services.ApiService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
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
        navigateToRegister();
    }
    
    private void redirectBasedOnRole(UserDTO user) {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Parent root;
            
            switch (user.getRole()) {
                case "STORE_MANAGER":
                    root = FXMLLoader.load(getClass().getResource("/com/lilach/client/views/store_manager.fxml"));
                    stage.setTitle("Store Manager Dashboard");
                    break;
                case "NETWORK_ADMIN":
                    root = FXMLLoader.load(getClass().getResource("/com/lilach/client/views/admin_dashboard.fxml"));
                    stage.setTitle("Network Admin Dashboard");
                    break;
                case "CUSTOMER":
                default:
                    root = FXMLLoader.load(getClass().getResource("/com/lilach/client/views/catalog.fxml"));
                    stage.setTitle("Lilach Flower Shop Catalog");
                    break;
            }
            
            stage.setScene(new Scene(root, 1200, 800));
            stage.centerOnScreen();
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to load view: " + e.getMessage());
        }
    }
    
    private void navigateToRegister() {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/com/lilach/client/views/register.fxml"));
            stage.setScene(new Scene(root, 800, 600));
            stage.centerOnScreen();
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to load register view: " + e.getMessage());
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