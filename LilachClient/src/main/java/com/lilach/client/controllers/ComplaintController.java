package com.lilach.client.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ComplaintController extends BaseController {
    @FXML private ComboBox<Integer> orderCombo;
    @FXML private TextArea descriptionArea;
    @FXML private Button submitButton;
    @FXML private Button backButton;
    
    @FXML
    public void initialize() {
        // Populate with sample orders (in real app, load from API)
        orderCombo.getItems().addAll(1001, 1002, 1003);
        orderCombo.getSelectionModel().selectFirst();
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
    private void handleSubmit() {
        if (validateForm()) {
            showSuccess("Complaint Submitted", "Your complaint has been submitted successfully!");
            navigateTo("/com/lilach/client/views/order_history.fxml", "My Orders");
        }
    }
    
    @FXML
    private void handleBack() {
        navigateTo("/com/lilach/client/views/order_history.fxml", "My Orders");
    }
    
    private boolean validateForm() {
        if (descriptionArea.getText().isEmpty()) {
            showError("Validation Error", "Please describe your complaint");
            return false;
        }
        return true;
    }
}