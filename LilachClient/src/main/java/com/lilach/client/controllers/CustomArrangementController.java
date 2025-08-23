package com.lilach.client.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class CustomArrangementController extends BaseController  {
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextArea flowerTypesField;
    @FXML private ComboBox<String> colorCombo;
    @FXML private ComboBox<String> priceCombo;
    @FXML private TextArea specialRequests;
    
    @FXML
    public void initialize() {
        // Set default selections
        typeCombo.getSelectionModel().selectFirst();
        colorCombo.getSelectionModel().selectFirst();
        priceCombo.getSelectionModel().selectFirst();
    }
    
    @FXML
    private void handleAddToCart() {
        if (validateForm()) {
            showSuccess("Added to Cartasdsd", "Your custom arrangement has been added to cart!");
            navigateToWithSize("/com/lilach/client/views/catalog.fxml", "Lilach Flower Shop Catalog", 1200, 800);        
        }
    }
    
    @FXML
    private void handleCancel() {
        navigateToWithSize("/com/lilach/client/views/catalog.fxml", "Lilach Flower Shop Catalog", 1200, 800);
    }
    
    private boolean validateForm() {
        if (flowerTypesField.getText().isEmpty()) {
            showError("Validation Error", "Please specify flower types");
            return false;
        }
        return true;
    }
    
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


}