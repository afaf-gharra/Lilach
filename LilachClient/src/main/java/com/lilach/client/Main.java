package com.lilach.client;

import org.kordamp.bootstrapfx.BootstrapFX;

import com.lilach.client.services.WebSocketService;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private static Stage primaryStage;
    
    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        
        // Load the init page first for server connection
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/lilach/client/views/init.fxml"));
        Parent root = loader.load();
        
        // Pass the stage to the controller
        com.lilach.client.controllers.InitController controller = loader.getController();
        controller.setStage(stage);
        
        Scene scene = new Scene(root, 500, 400);
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        
        primaryStage.setTitle("Lilach Flower Shop - Server Connection");
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();

        // WebSocket will connect when server URL is set in InitController
    }

    @Override
    public void stop() throws Exception {
        // Clean up WebSocket connection when app closes
        WebSocketService.disconnect();
        super.stop();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}