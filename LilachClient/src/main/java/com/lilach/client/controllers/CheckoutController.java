package com.lilach.client.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

public class CheckoutController extends BaseController  {
    @FXML private DatePicker deliveryDatePicker;
    @FXML private TextField deliveryTimeField;
    @FXML private TextArea deliveryAddress;
    @FXML private TextField recipientName;
    @FXML private TextField recipientPhone;
    @FXML private TextArea greetingMessage;
    @FXML private ListView<String> orderItems;
    @FXML private Label subtotalLabel;
    @FXML private Label totalLabel;
    
    private double subtotal = 125.0; // Would come from cart
    
    @FXML
    public void initialize() {
        // Set default delivery date (tomorrow)
        deliveryDatePicker.setValue(LocalDate.now().plusDays(1));
        
        // Set sample order items
        ObservableList<String> items = FXCollections.observableArrayList(
            "Red Roses Bouquet - $45.00",
            "Custom Arrangement - $80.00"
        );
        orderItems.setItems(items);
        
        // Set prices
        subtotalLabel.setText(String.format("$%.2f", subtotal));
        totalLabel.setText(String.format("$%.2f", subtotal + 10.00));
    }
    
    @FXML
    private void handlePlaceOrder() {
        if (validateForm()) {
            // In a real app, this would create the order
            showSuccess("Order Placed", "Your order has been placed successfully!");
            navigateToOrderConfirmation();
        }
    }
    
    private boolean validateForm() {
        if (deliveryDatePicker.getValue() == null) {
            showError("Validation Error", "Please select delivery date");
            return false;
        }
        if (!deliveryTimeField.getText().matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            showError("Validation Error", "Please enter valid time in HH:mm format");
            return false;
        }
        if (deliveryAddress.getText().isEmpty()) {
            showError("Validation Error", "Delivery address is required");
            return false;
        }
        if (recipientName.getText().isEmpty()) {
            showError("Validation Error", "Recipient name is required");
            return false;
        }
        return true;
    }
    
    private void navigateToOrderConfirmation() {
        // Would navigate to order confirmation page
        System.out.println("Would navigate to confirmation page");
    }
    

}