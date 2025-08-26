package com.lilach.client.controllers;

import com.lilach.client.Main;
import com.lilach.client.models.UserDTO;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;

import javafx.stage.Stage;

import java.io.IOException;

public abstract  class BaseController {
    

    public UserDTO loggedInUser;
    @FXML
    private void handleLogout() {
        logout();
    }

    @FXML
    private void handleViewCart() {
        navigateTo("/com/lilach/client/views/cart.fxml", "Shopping Cart");
    }
    
    @FXML
    private void handleViewOrders() {
        navigateTo("/com/lilach/client/views/order_history.fxml", "My Orders");
    }

    @FXML
    private void navigateToCatalog() {
        navigateTo("/com/lilach/client/views/catalog.fxml", "Catalog");
    }

    public UserDTO getLoggedInUser() {
        if (loggedInUser == null) {
            showError("Authentication Required", "Please log in to continue.");
            navigateTo("/com/lilach/client/views/login.fxml", "Login");
            
        }
        return loggedInUser;
    }

    protected void navigateTo(String fxmlPath, String title) {
        try {
            Stage stage = Main.getPrimaryStage();
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle(title);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Navigation Error", "Failed to load view: " + e.toString());
            System.err.println("Navigation error to " + fxmlPath + ": " + e.toString());
        }
    }
    
    protected void navigateToWithSize(String fxmlPath, String title, int width, int height) {
        try {
            Stage stage = Main.getPrimaryStage();
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            stage.setScene(new Scene(root, width, height));
            stage.setTitle(title);
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Navigation Error", "Failed to load view: " + e.getMessage());
        }
    }
    
    protected void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    protected void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    protected void logout() {
        try {
            Stage stage = Main.getPrimaryStage();
            Parent root = FXMLLoader.load(getClass().getResource("/com/lilach/client/views/login.fxml"));
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Lilach Flower Shop Login");
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Navigation Error", "Failed to load login view: " + e.getMessage());
        }
    }
}