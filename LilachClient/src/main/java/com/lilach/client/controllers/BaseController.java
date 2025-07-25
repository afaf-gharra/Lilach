package com.lilach.client.controllers;

import com.lilach.client.Main;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;

import javafx.stage.Stage;

import java.io.IOException;

public abstract  class BaseController {
    


    

    protected void navigateTo(String fxmlPath, String title) {
        try {
            Stage stage = Main.getPrimaryStage();
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.centerOnScreen();
        } catch (Exception e) {
            showError("Navigation Error", "Failed to load view: " + e.getMessage());
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
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Lilach Flower Shop Login");
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Navigation Error", "Failed to load login view: " + e.getMessage());
        }
    }
}