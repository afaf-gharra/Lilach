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
            showSuccess("Added to Cart", "Your custom arrangement has been added to cart!");
            navigateToCatalog();
        }
    }
    
    @FXML
    private void handleCancel() {
        navigateToCatalog();
    }
    
    private boolean validateForm() {
        if (flowerTypesField.getText().isEmpty()) {
            showError("Validation Error", "Please specify flower types");
            return false;
        }
        return true;
    }
    
    

}