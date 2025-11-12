package com.lilach.client.controllers;

import java.io.IOException;

import com.lilach.client.Main;
import com.lilach.client.models.UserDTO;
import com.lilach.client.services.ApiService;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public abstract  class BaseController {
    

    public static UserDTO loggedInUser;
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
        Platform.runLater(() -> {
            showError("Authentication Required", "Please log in to continue.");
            navigateTo("/com/lilach/client/views/login.fxml", "Login");
        });
        return null;
    }
    return loggedInUser;
}
    protected void navigateTo(String fxmlPath, String title) {
        try {
            Stage stage = Main.getPrimaryStage();
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            stage.setScene(new Scene(root, 1600, 900));
            stage.setTitle(title);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
           // showError("Navigation Error", "Failed to load view: " + e.toString());
            System.err.println("Navigation error to " + fxmlPath + ": " + e.toString());
        }
    }
    
    protected void navigateToWithSize(String fxmlPath, String title, int width, int height) {
        try {
            Stage stage = Main.getPrimaryStage();
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            stage.setScene(new Scene(root, 1600, 900));
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

    //logout

    protected void navigateToLogin() {
        navigateTo("/com/lilach/client/views/login.fxml", "Login");
    }
    
    protected void logout() {
        // Best-effort notify server to mark user offline, then clear local session and navigate to login
        if (loggedInUser != null) {
            try {
                ApiService.logout(loggedInUser.getId());
            } catch (Exception e) {
                // ignore network errors; proceed to clear local session
            }
            loggedInUser = null;
        }

        try {
            Stage stage = Main.getPrimaryStage();
            Parent root = FXMLLoader.load(getClass().getResource("/com/lilach/client/views/login.fxml"));
            stage.setScene(new Scene(root, 1600, 900));
            stage.setTitle("Lilach Flower Shop Login");
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Navigation Error", "Failed to load login view: " + e.getMessage());
        }
    }
}