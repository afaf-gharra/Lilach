package com.lilach.client.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class BaseController {
    protected void navigateTo(String fxmlPath, String title) {
        try {
            Stage stage = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            showError("Navigation Error", "Failed to load view: " + e.getMessage());
        }
    }
    
    protected void navigateToCatalog() {
        try {
            Stage stage = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource("/com/lilach/client/views/catalog.fxml"));
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Lilach Flower Shop Catalog");
            stage.show();
        } catch (IOException e) {
            showError("Navigation Error", "Failed to load catalog view: " + e.getMessage());
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
}