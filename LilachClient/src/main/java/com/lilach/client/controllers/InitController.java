package com.lilach.client.controllers;

import org.kordamp.bootstrapfx.BootstrapFX;

import com.lilach.client.services.ApiService;
import com.lilach.client.services.WebSocketService;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class InitController {

    @FXML private TextField ipField;
    @FXML private TextField portField;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        // Set default values
        ipField.setText("localhost");
        portField.setText("8080");
    }

    @FXML
    private void connect() {
        String ip = ipField.getText().trim();
        String portText = portField.getText().trim();

        if (ip.isEmpty() || portText.isEmpty()) {
            showError("Please enter both IP and Port.");
            return;
        }

        try {
            int port = Integer.parseInt(portText);
            
            // Set the base URL in ApiService
            String baseUrl = "http://" + ip + ":" + port + "/api/";
            ApiService.setBaseUrl(baseUrl);
            
            // Test connection by trying to fetch products
            try {
                ApiService.getProducts();
                
                // Connection successful, connect WebSocket
                String wsServerUrl = "http://" + ip + ":" + port;
                WebSocketService.connect(wsServerUrl);
                
                // Load catalog
                loadCatalog();
                
            } catch (Exception e) {
                showError("Failed to connect to server at " + ip + ":" + port + "\nPlease check the server is running.");
                e.printStackTrace();
            }

        } catch (NumberFormatException e) {
            showError("Invalid port number.");
        }
    }

    private void loadCatalog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/lilach/client/views/catalog.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 1600, 900);
            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            scene.getStylesheets().add(getClass().getResource("/com/lilach/client/css/styles.css").toExternalForm());
            
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load catalog view.");
        }
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, message);
            alert.showAndWait();
        });
    }
}
