package com.lilach.client.controllers;

import com.lilach.client.services.ApiService;
import com.lilach.client.models.UserDTO;
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

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    
    @FXML
    public void initialize() {
        loginButton.setGraphic(new FontIcon("fas-sign-in-alt"));
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
            if (user != null && user.isActive()) {
                navigateToCatalog();
            } else {
                showAlert("Login Failed", "Invalid credentials or inactive account");
            }
        } catch (IOException e) {
            showAlert("Connection Error", "Failed to connect to server: " + e.getMessage());
        }
    }
    
    private void navigateToCatalog() {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/com/lilach/client/views/catalog.fxml"));
            stage.setScene(new Scene(root, 1000, 700));
            stage.centerOnScreen();
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to load catalog view: " + e.getMessage());
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